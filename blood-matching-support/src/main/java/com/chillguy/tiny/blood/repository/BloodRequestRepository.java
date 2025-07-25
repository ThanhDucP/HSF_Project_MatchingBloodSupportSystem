package com.chillguy.tiny.blood.repository;

import com.chillguy.tiny.blood.entity.Blood;
import com.chillguy.tiny.blood.entity.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BloodRequestRepository extends JpaRepository<BloodRequest,String> {

    Optional<BloodRequest> findByIdBloodRequest(String idBloodRequest);

    List<BloodRequest> findByPatientNameContainingIgnoreCaseAndStatusInAndBloodCodeBloodTypeInAndBloodCodeRhIn(String patientName, List<BloodRequest.Status> statuses, List<Blood.BloodType> bloodType, List<Blood.RhFactor> bloodCodeRh);

    List<BloodRequest> findByPatientNameContainingIgnoreCaseAndStatusInAndBloodCodeBloodTypeInAndBloodCodeRhInAndAccountAccountId(String patientName, List<BloodRequest.Status> statuses, List<Blood.BloodType> bloodType, List<Blood.RhFactor> bloodCodeRh, String accountId);



    Collection<BloodRequest> findByAccount_AccountId(String accountAccountId);

    boolean existsByAccount_AccountIdAndStatusNotIn(String accountAccountId, Collection<BloodRequest.Status> statuses);
}
