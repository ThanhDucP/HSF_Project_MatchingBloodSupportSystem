package com.chillguy.tiny.blood.repository;

import com.chillguy.tiny.blood.entity.BloodRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface BloodRequestRepository extends JpaRepository<BloodRequest,String> {

        Optional<BloodRequest> findByIdBloodRequest(String idBloodRequest);

        boolean existsByAccount_AccountIdAndStatusIn(String accountAccountId, Collection<BloodRequest.Status> statuses);

    Collection<BloodRequest> findByAccount_AccountId(String accountAccountId);
}
