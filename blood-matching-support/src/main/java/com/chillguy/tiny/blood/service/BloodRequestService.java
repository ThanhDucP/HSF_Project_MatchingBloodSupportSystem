package com.chillguy.tiny.blood.service;

import com.chillguy.tiny.blood.dto.BloodRequestDTO;
import com.chillguy.tiny.blood.dto.BloodRequestResponseDTO;
import com.chillguy.tiny.blood.dto.ConfirmDonationDTO;
import com.chillguy.tiny.blood.entity.*;
import com.chillguy.tiny.blood.repository.AccountRepository;
import com.chillguy.tiny.blood.repository.BloodRepository;
import com.chillguy.tiny.blood.repository.BloodRequestRepository;
import com.chillguy.tiny.blood.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BloodRequestService {

    private final BloodRequestRepository requestRepo;
    private final AccountRepository accountRepo;
    private final ProfileRepository profileRepo;
    private final EmailNotifier emailNotifier;
    private final BloodRepository bloodRepo;
    private final BloodRequestRepository bloodRequestRepository;

    public List<BloodRequestResponseDTO> getAllRequests() {
        return requestRepo.findAll().stream().map(request ->
                BloodRequestResponseDTO.builder()
                        .requestId(request.getIdBloodRequest())
                        .patientName(request.getPatientName())
                        .requestDate(request.getRequestDate())
                        .bloodCode(request.getBloodCode().getBloodCode())
                        .isEmergency(request.isEmergency())
                        .volume(request.getVolume())
                        .status(request.getStatus().name())
                        .confirmedCount(request.getConfirmedCount())
                        .build()
        ).toList();
    }



    public BloodRequestResponseDTO createRequest(BloodRequestDTO dto, String accountId) {
        Account acc = accountRepo.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với accountId: " + accountId));
        Blood blood = bloodRepo.findByBloodCode(dto.getBloodCode());

        boolean hasUnfinishedRequest = requestRepo
                .existsByAccount_AccountIdAndStatusIn(accountId, List.of(
                        BloodRequest.Status.PENDING,
                        BloodRequest.Status.MATCHED,
                        BloodRequest.Status.CONFIRMED
                ));

        if (hasUnfinishedRequest) {
            throw new IllegalStateException("Bạn đã có đơn xin máu chưa hoàn thành.");
        }

        BloodRequest request = BloodRequest.builder()
                .idBloodRequest(UUID.randomUUID().toString())
                .account(acc)
                .patientName(dto.getPatientName())
                .requestDate(dto.getRequestDate())
                .bloodCode(blood)
                .volume(dto.getVolume())
                .status(BloodRequest.Status.PENDING)
                .isEmergency(dto.isEmergency())
                .requestCreationDate(LocalDate.now())
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




    public void matchAndNotify(String requestId) {
        BloodRequest request = requestRepo.findByIdBloodRequest(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn yêu cầu với ID: " + requestId));

        String recipientCode = request.getBloodCode().getBloodCodeString();

        List<Profile> allProfiles = profileRepo.findAll();
        List<Profile> eligible = allProfiles.stream()
                .filter(this::isEligibleToDonate)
                .filter(p -> BloodCompatibility.canDonateTo(
                        p.getBloodCode().getBloodCodeString(), recipientCode))
                .sorted(Comparator.comparing(p -> distance(request, p)))
                .limit(20)
                .toList();

        if (eligible.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy người hiến máu phù hợp.");
        }

        List<String> emails = eligible.stream().map(p -> p.getAccount().getEmail()).toList();
        String body = "🩸 Bạn có thể giúp hiến máu cho bệnh nhân " + request.getPatientName()
                + ". Xác nhận tại: http://localhost:8080/api/blood-requests/confirm?id=" + requestId;

        emailNotifier.sendToMany(emails, "[KHẨN] Cần máu gấp", body);

        request.setConfirmedCount(request.getConfirmedCount() + 1);
        if(request.getConfirmedCount() >= 3) {}
        request.setStatus(BloodRequest.Status.MATCHED);
        requestRepo.save(request);
    }

    public void updateStatus(String requestId, BloodRequest.Status newStatus) {
        BloodRequest request = bloodRequestRepository.findByIdBloodRequest(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn yêu cầu với ID: " + requestId));


        if (request.getStatus() == BloodRequest.Status.COMPLETED || request.getStatus() == BloodRequest.Status.CANCELLED) {
            throw new IllegalStateException("Không thể cập nhật trạng thái của đơn đã hoàn tất hoặc bị huỷ");
        }

        request.setStatus(newStatus);
        bloodRequestRepository.save(request);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void retryMatchAndNotifyAll() {
        List<BloodRequest> pendingRequests = requestRepo.findAll().stream()
                .filter(req -> !req.isClosed() && req.getConfirmedCount() < 3)
                .toList();

        for (BloodRequest request : pendingRequests) {
            try {
                long daysPassed = ChronoUnit.DAYS.between(request.getRequestDate(), LocalDate.now());

                // Chỉ gửi lại sau 2, 4, 6,... ngày
                if (daysPassed < 2 || daysPassed % 2 != 0) continue;

                int batch = (int) (daysPassed / 2); // mỗi 2 ngày tăng 1 lô
                int start = batch * 20;
                int end = start + 20;

                String recipientCode = request.getBloodCode().getBloodCodeString();

                List<Profile> eligible = profileRepo.findAll().stream()
                        .filter(this::isEligibleToDonate)
                        .filter(p -> BloodCompatibility.canDonateTo(
                                p.getBloodCode().getBloodCodeString(), recipientCode))
                        .sorted(Comparator.comparing(p -> distance(request, p)))
                        .toList();

                if (eligible.size() <= start) continue;

                List<Profile> batchProfiles = eligible.subList(start, Math.min(end, eligible.size()));
                List<String> emails = batchProfiles.stream()
                        .map(p -> p.getAccount().getEmail())
                        .filter(email -> email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
                        .toList();

                if (emails.isEmpty()) continue;

                String body = "🩸 Đây là lời nhắc: Bạn có thể giúp hiến máu cho bệnh nhân " + request.getPatientName()
                        + ". Xác nhận tại: http://localhost:8080/api/blood-requests/confirm?id=" + request.getIdBloodRequest();

                emailNotifier.sendToMany(emails, "[NHẮC LẠI] Cần máu gấp", body);

                System.out.println("Đã gửi lại batch " + batch + " cho request " + request.getIdBloodRequest());
            } catch (Exception e) {
                System.err.println("Lỗi khi retry request " + request.getIdBloodRequest() + ": " + e.getMessage());
            }
        }
    }


    public String confirmDonor(ConfirmDonationDTO confirmDTO) {
        BloodRequest req = requestRepo.findByIdBloodRequest(confirmDTO.getRequestId()).orElseThrow();
        if (req.isClosed()) return "Đơn đã đủ người.";
        req.setConfirmedCount(req.getConfirmedCount() + 1);
        if (req.getConfirmedCount() >= 3) {
            req.setStatus(BloodRequest.Status.CONFIRMED);
            req.setClosed(true);
            emailNotifier.send(req.getAccount().getEmail(), "Đơn máu đã đủ", "Đơn máu đã có 3 người xác nhận.");
        }
        requestRepo.save(req);
        return "Đã xác nhận thành công";
    }

    private boolean isEligibleToDonate(Profile p) {
        return p.getRestDate() == null || p.getRestDate().isBefore(LocalDate.now());
    }

    private double distance(BloodRequest request, Profile profile) {
        double lat1 = request.getAccount().getProfile().getAddress().getLatitude();
        double lon1 = request.getAccount().getProfile().getAddress().getLongitude();
        double lat2 = profile.getAddress().getLatitude();
        double lon2 = profile.getAddress().getLongitude();
        return haversine(lat1, lon1, lat2, lon2);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
