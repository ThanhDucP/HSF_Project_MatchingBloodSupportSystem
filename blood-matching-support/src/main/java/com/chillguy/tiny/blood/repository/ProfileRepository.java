package com.chillguy.tiny.blood.repository;

import com.chillguy.tiny.blood.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query("SELECT p FROM Profile p WHERE " +
            "p.restDate < CURRENT_DATE " +
            "AND CONCAT(p.bloodCode.bloodType, CASE WHEN p.bloodCode.rh = 'POSITIVE' THEN '+' ELSE '-' END) IN :compatibleCodes")
    List<Profile> findEligibleProfiles(@Param("compatibleCodes") List<String> compatibleCodes);


}
