package com.chillguy.tiny.blood.repository;

import com.chillguy.tiny.blood.entity.Blood;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodRepository extends JpaRepository<Blood, Long> {

    Blood findByBloodCode(String bloodCode);

    int countAvailableBloodByBloodCode(String bloodCode);
}
