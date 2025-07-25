package com.chillguy.tiny.blood.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.chillguy.tiny.blood.dto.BloodRequestDTO;
import com.chillguy.tiny.blood.dto.BloodRequestResponseDTO;
import com.chillguy.tiny.blood.entity.Account;
import com.chillguy.tiny.blood.entity.Blood;
import com.chillguy.tiny.blood.entity.BloodCompatibility;
import com.chillguy.tiny.blood.entity.BloodRequest;
import com.chillguy.tiny.blood.entity.Profile;
import com.chillguy.tiny.blood.repository.AccountRepository;
import com.chillguy.tiny.blood.repository.BloodRepository;
import com.chillguy.tiny.blood.repository.BloodRequestRepository;
import com.chillguy.tiny.blood.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BloodRequestService {

    private final BloodRequestRepository requestRepo;
    private final AccountRepository accountRepo;
    private final ProfileRepository profileRepo;
    private final EmailNotifier emailNotifier;
    private final BloodRepository bloodRepo;

    public BloodRequestResponseDTO createRequest(BloodRequestDTO dto, String accountId) {
        System.out.println("DEBUG - Tìm accountId: " + accountId);
        Account acc = accountRepo.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với accountId: " + accountId));
        profileRepo.findProfileByAccount(acc)
                .orElseThrow(() -> new IllegalArgumentException("Bạn chưa có thông tin cá nhân, vui lòng tạo!!"));
        
        System.out.println("DEBUG - Tìm bloodCode: " + dto.getBloodCode());

        // Convert frontend format to database format
        // Frontend: A_POSITIVE_RED_BLOOD_CELL → Database: A_POS_RED_BLOOD_CELL
        String dbBloodCode = dto.getBloodCode();


        Blood blood = bloodRepo.findByBloodCode(dbBloodCode);
        System.out.println("DEBUG - Kết quả blood: " + (blood != null ? blood.getBloodCode() : "NULL"));

        if (blood == null) {
            throw new IllegalArgumentException("Không tìm thấy loại máu với mã: " + dto.getBloodCode() + " (converted: " + dbBloodCode + ")");
        }

        boolean hasUnfinished = requestRepo.existsByAccount_AccountIdAndStatusNotIn(
                accountId,
                List.of(BloodRequest.Status.CONFIRMED, BloodRequest.Status.CANCELLED)
        );

        if (hasUnfinished) throw new IllegalStateException("Bạn đã có đơn xin máu chưa hoàn thành.");

        BloodRequest request = BloodRequest.builder()
                .idBloodRequest(UUID.randomUUID().toString())
                .account(acc)
                .patientName(dto.getPatientName())
                .requestDate(dto.getRequestDate())
                .bloodCode(blood)
                .volume(dto.getVolume())
                .isEmergency(dto.isEmergency())
                .requestCreationDate(LocalDate.now())
                .status(BloodRequest.Status.PENDING)
                .confirmedCount(0)
                .isClosed(false)
                .build();

        requestRepo.save(request);

        return BloodRequestResponseDTO.builder()
                .requestId(request.getIdBloodRequest())
                .patientName(request.getPatientName())
                .requestDate(request.getRequestDate())
                .bloodCode(request.getBloodCode().getBloodCode())
                .isEmergency(request.isEmergency())
                .volume(request.getVolume())
                .status(request.getStatus().name())
                .confirmedCount(request.getConfirmedCount())
                .build();
    }

    public void processRequestWithInventoryCheck(String requestId) {
        BloodRequest request = requestRepo.findByIdBloodRequest(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn yêu cầu với ID: " + requestId));

        int stock = bloodRepo.countAvailableBloodByBloodCode(request.getBloodCode().getBloodCode());
        if (stock < request.getVolume() + 10) {
            matchInitialBatch(request);
        } else {
            String body = "<p>🩸 Đơn máu cho bệnh nhân <strong>" + request.getPatientName() + "</strong> đã sẵn sàng.</p>"
                    + "<p>Vui lòng đến cơ sở y tế gần nhất để nhận máu.</p>";
            emailNotifier.sendHtml(request.getAccount().getEmail(), "[THÀNH CÔNG] Đơn máu đã được cấp", body);
            request.setStatus(BloodRequest.Status.CONFIRMED);
            request.setClosed(true);
            requestRepo.save(request);
        }
    }

    private void matchInitialBatch(BloodRequest request) {
        List<Profile> eligible = findEligibleDonors(request, 0, 20);
        if (eligible.isEmpty()) throw new IllegalStateException("Không tìm thấy người hiến phù hợp.");
        sendMatchingBatch(request, eligible, "[KHẨN] Cần máu gấp", "Bạn có thể giúp hiến máu cho bệnh nhân");
        request.setStatus(BloodRequest.Status.MATCHED);
        requestRepo.save(request);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void retryMatchAndNotifyAll() {
        List<BloodRequest> requests = requestRepo.findAll().stream()
                .filter(req -> !req.isClosed() && req.getConfirmedCount() < 3)
                .toList();

        for (BloodRequest request : requests) {
            try {
                long daysPassed = ChronoUnit.DAYS.between(request.getRequestCreationDate(), LocalDate.now());
                if (daysPassed < 2 || daysPassed % 2 != 0) continue;

                int batch = (int) (daysPassed / 2);
                List<Profile> donors = findEligibleDonors(request, batch * 20, (batch + 1) * 20);

                if (donors.isEmpty()) continue;

                sendMatchingBatch(request, donors, "[NHẮC LẠI] Cần máu gấp", "Lời nhắc: Bạn có thể giúp hiến máu cho bệnh nhân");
                requestRepo.save(request);
                System.out.println("Đã gửi batch " + batch + " cho đơn " + request.getIdBloodRequest());

            } catch (Exception e) {
                System.err.println("Lỗi khi retry đơn " + request.getIdBloodRequest() + ": " + e.getMessage());
            }
        }
    }

    private void sendMatchingBatch(BloodRequest request, List<Profile> donors, String subject, String message) {
        for (Profile profile : donors) {
            String email = profile.getAccount().getEmail();
            if (!isValidEmail(email)) continue;

            String token = UUID.randomUUID().toString();
            request.addConfirmationToken(token, email);

            String link = "http://localhost:8080/blood-requests/confirm-by-token?requestId="
                    + request.getIdBloodRequest() + "&token=" + token;

            String body = "<p>🩸 " + message + " <strong>" + request.getPatientName() + "</strong>.</p>"
                    + "<p><a href=\"" + link + "\" style=\"padding:10px 20px; background-color:#d32f2f; color:white; text-decoration:none;\">XÁC NHẬN HIẾN MÁU</a></p>";

            emailNotifier.sendHtml(email, subject, body);
        }
    }

    private List<Profile> findEligibleDonors(BloodRequest request, int start, int end) {
        String recipientCode = request.getBloodCode().getBloodCodeString();

        List<Profile> eligible = profileRepo.findAll().stream()
                .filter(p -> isEligibleToDonate(p, request.getRequestDate()))
                .filter(p -> !p.getAccount().getAccountId().equals(request.getAccount().getAccountId()))
                .filter(p -> BloodCompatibility.canDonateTo(
                        p.getBloodCode().getBloodCodeString(), recipientCode))
                .filter(p -> p.getBloodCode().getComponentType() == request.getBloodCode().getComponentType())
                .sorted(Comparator.comparing(p -> distance(request, p)))
                .toList();

        return eligible.subList(Math.min(start, eligible.size()), Math.min(end, eligible.size()));
    }


    private boolean isEligibleToDonate(Profile p, LocalDate restDate) {
        return p.getRestDate() == null || !p.getRestDate().isAfter(restDate);
    }


    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    private double distance(BloodRequest request, Profile profile) {
        double lat1 = request.getAccount().getProfile().getAddress().getLatitude();
        double lon1 = request.getAccount().getProfile().getAddress().getLongitude();
        double lat2 = profile.getAddress().getLatitude();
        double lon2 = profile.getAddress().getLongitude();
        return haversine(lat1, lon1, lat2, lon2);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public List<BloodRequestResponseDTO> getAllRequestsByAccountId(
        String patientName,
        List<BloodRequest.Status> statuses,
        List<Blood.BloodType> bloodType,
        List<Blood.RhFactor> bloodCodeRh,
        String accountId
    ){
        return requestRepo.findByPatientNameContainingIgnoreCaseAndStatusInAndBloodCodeBloodTypeInAndBloodCodeRhInAndAccountAccountId(
                patientName, statuses, bloodType, bloodCodeRh, accountId
        ).stream()
                .sorted(Comparator.comparing(BloodRequest::getRequestCreationDate).reversed())
                .map(request -> BloodRequestResponseDTO.builder()
                        .requestId(request.getIdBloodRequest())
                        .patientName(request.getPatientName())
                        .requestDate(request.getRequestDate())
                        .bloodCode(request.getBloodCode().getBloodCode())
                        .bloodType(request.getBloodCode().getBloodType().name())
                        .rhFactor(request.getBloodCode().getRh().name())
                        .isEmergency(request.isEmergency())
                        .volume(request.getVolume())
                        .status(request.getStatus().name())
                        .confirmedCount(request.getConfirmedCount())
                        .build())
                .toList();
    }


    public List<BloodRequestResponseDTO> getAllRequests(
        String patientName,
        List<BloodRequest.Status> statuses,
        List<Blood.BloodType> bloodType,
        List<Blood.RhFactor> bloodCodeRh
    ) {
        return requestRepo.findByPatientNameContainingIgnoreCaseAndStatusInAndBloodCodeBloodTypeInAndBloodCodeRhIn(
                patientName, statuses, bloodType, bloodCodeRh
        ).stream()
                .sorted(Comparator.comparing(BloodRequest::getRequestCreationDate).reversed())
                .map(request -> BloodRequestResponseDTO.builder()
                        .requestId(request.getIdBloodRequest())
                        .patientName(request.getPatientName())
                        .requestDate(request.getRequestDate())
                        .bloodCode(request.getBloodCode().getBloodCode())
                        .bloodType(request.getBloodCode().getBloodType().name())
                        .rhFactor(request.getBloodCode().getRh().name())
                        .isEmergency(request.isEmergency())
                        .volume(request.getVolume())
                        .status(request.getStatus().name())
                        .confirmedCount(request.getConfirmedCount())
                        .build())
                .toList();
    }

    public void updateStatus(String requestId, BloodRequest.Status newStatus) {
        BloodRequest request = requestRepo.findByIdBloodRequest(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn yêu cầu với ID: " + requestId));

        if (request.getStatus() == BloodRequest.Status.CONFIRMED || request.getStatus() == BloodRequest.Status.CANCELLED) {
            throw new IllegalStateException("Không thể cập nhật trạng thái của đơn đã hoàn tất hoặc bị huỷ.");
        }

        request.setStatus(newStatus);

        if (newStatus == BloodRequest.Status.CONFIRMED) {
            request.setClosed(true);
        }

        requestRepo.save(request);
    }
    @Scheduled(cron = "0 0 0 * * *")
    public void autoCancelExpiredRequests() {
        List<BloodRequest> expired = requestRepo.findAll().stream()
                .filter(r -> !r.isClosed())
                .filter(r -> ChronoUnit.DAYS.between(r.getRequestCreationDate(), LocalDate.now()) > 7)
                .toList();

        for (BloodRequest r : expired) {
            r.setStatus(BloodRequest.Status.CANCELLED);
            r.setClosed(true);
            requestRepo.save(r);

            emailNotifier.send(
                    r.getAccount().getEmail(),
                    "[HUỶ] Đơn máu đã quá hạn",
                    "Đơn yêu cầu máu cho bệnh nhân " + r.getPatientName() +
                            " đã bị huỷ do quá 7 ngày mà chưa hoàn thành."
            );
        }
    }

    public List<BloodRequestResponseDTO> getByAccountId(String accountId) {
        return requestRepo.findByAccount_AccountId(accountId).stream()
                .sorted(Comparator.comparing(BloodRequest::getRequestCreationDate).reversed())
                .map(this::toDTO)
                .toList();
    }


    private BloodRequestResponseDTO toDTO(BloodRequest request) {
        return BloodRequestResponseDTO.builder()
                .requestId(request.getIdBloodRequest())
                .patientName(request.getPatientName())
                .requestDate(request.getRequestDate())
                .bloodCode(request.getBloodCode().getBloodCode())
                .bloodType(request.getBloodCode().getBloodType().name())
                .rhFactor(request.getBloodCode().getRh().name())
                .isEmergency(request.isEmergency())
                .volume(request.getVolume())
                .status(request.getStatus().name())
                .confirmedCount(request.getConfirmedCount())
                .build();
    }


}
