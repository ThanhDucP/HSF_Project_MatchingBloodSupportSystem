package com.chillguy.tiny.blood.repository;

import com.chillguy.tiny.blood.entity.Blood;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodRepository extends JpaRepository<Blood, String> {

    Blood findByBloodCode(String bloodCode);

    int countAvailableBloodByBloodCode(String bloodCode);

    Optional<Blood> findByBloodTypeAndRh(Blood.BloodType bloodType, Blood.RhFactor rhFactor);
}
