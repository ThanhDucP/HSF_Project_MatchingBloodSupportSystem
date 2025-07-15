package com.chillguy.tiny.blood.mapper;

import com.chillguy.tiny.blood.dto.response.ProfileDto;
import com.chillguy.tiny.blood.dto.response.ProfileUpdateResponseDto;
import com.chillguy.tiny.blood.entity.Profile;

public class ProfileMapper {
    public static ProfileUpdateResponseDto profileUpdateMapper(Profile profile) {

        if (profile == null) return null;

        ProfileUpdateResponseDto profileUpdateResponseDto = new ProfileUpdateResponseDto();

        profileUpdateResponseDto.setName(profile.getName());
        profileUpdateResponseDto.setPhone(profile.getPhone());
        profileUpdateResponseDto.setDob(profile.getDob());

        return profileUpdateResponseDto;
    }

    public static ProfileDto profileMapper(Profile profile) {

        if (profile == null) return null;
        ProfileDto ProfileDto = new ProfileDto();

        if (profile.getName() != null)
            ProfileDto.setName(profile.getName());
        if (profile.getPhone() != null)
            ProfileDto.setPhone(profile.getPhone());
        if (profile.getDob() != null)
            ProfileDto.setDob(profile.getDob());
        if (profile.getGender() != null)
            ProfileDto.setGender(profile.getGender());
        if (profile.getAddress() != null)
            ProfileDto.setAddress(AddressMapper.addressMapper(profile.getAddress()));
        if (profile.getNumberOfBloodDonation() != null)
            ProfileDto.setNumberOfBloodDonation(profile.getNumberOfBloodDonation());
        if (profile.getBloodCode() != null)
            ProfileDto.setBlood(BloodMapper.bloodMapper(profile.getBloodCode()));
        if (profile.getRestDate() != null)
            ProfileDto.setRestDate(profile.getRestDate());

        return ProfileDto;
    }
}
