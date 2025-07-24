package com.chillguy.tiny.blood.service;

import com.chillguy.tiny.blood.dto.ProfileUpdateDto;
import com.chillguy.tiny.blood.dto.response.ProfileDto;

public interface IProfileService {
    ProfileDto saveProfile(String accountId, ProfileDto requestDto);
    ProfileDto getProfileByAccountId(String accountId);
    ProfileDto updateProfile(String accountId, ProfileUpdateDto requestDto);
}
