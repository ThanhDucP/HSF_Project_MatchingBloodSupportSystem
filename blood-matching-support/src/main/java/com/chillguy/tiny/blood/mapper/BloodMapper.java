package com.chillguy.tiny.blood.mapper;

import com.chillguy.tiny.blood.dto.BloodDto;
import com.chillguy.tiny.blood.entity.Blood;

public class BloodMapper {
    public static BloodDto bloodMapper(Blood bloodCode) {
        BloodDto bloodDto = new BloodDto();
        bloodDto.setBloodType(bloodCode.getBloodCode());
        bloodDto.setRhFactor(bloodCode.getRh().name());
        bloodDto.setComponentType(bloodCode.getComponentType().name());
        return bloodDto;
    }

    public static Blood bloodMapper(BloodDto bloodDto) {
        Blood blood = new Blood();
        blood.setBloodType(Blood.BloodType.valueOf(bloodDto.getBloodType()));
        blood.setRh(Blood.RhFactor.valueOf(bloodDto.getRhFactor()));
        blood.setComponentType(Blood.ComponentType.valueOf(bloodDto.getComponentType()));
        return blood;
    }
}
