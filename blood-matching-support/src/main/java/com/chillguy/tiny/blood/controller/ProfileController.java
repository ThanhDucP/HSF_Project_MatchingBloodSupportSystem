package com.chillguy.tiny.blood.controller;

import com.chillguy.tiny.blood.dto.ProfileUpdateDto;
import com.chillguy.tiny.blood.dto.response.ApiResponse;
import com.chillguy.tiny.blood.dto.response.ProfileDto;
import com.chillguy.tiny.blood.service.IProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final IProfileService profileService;

    public ProfileController(IProfileService profileService) {
        this.profileService = profileService;
    }


    @PreAuthorize("hasRole('ADMIN') or (hasRole('MEMBER') and #accountId == authentication.principal.accountId)")
    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<ProfileDto>> getProfile(@PathVariable("accountId") String accountId) {

        ProfileDto responseDto = profileService.getProfileByAccountId(accountId);
        return ResponseEntity.ok(ApiResponse.<ProfileDto>builder()
                .code(200)
                .message("Success")
                .result(responseDto)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('MEMBER') and #accountId == authentication.principal.accountId)")
    @PostMapping("/{accountId}")
    public ResponseEntity<ApiResponse<ProfileDto>> createProfile(@PathVariable String accountId, @RequestBody ProfileDto requestDto) {
        ProfileDto createdProfileDto = profileService.createProfile(accountId, requestDto);
        return ResponseEntity.ok(ApiResponse.<ProfileDto>builder()
                .code(200)
                .message("Created profile successfully")
                .result(createdProfileDto)
                .build());
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('MEMBER') and #accountId == authentication.principal.accountId)")
    @PutMapping("/{accountId}")
    public ResponseEntity<ApiResponse<ProfileDto>> updateProfile(@PathVariable String accountId, @RequestBody ProfileUpdateDto requestDto) {
        ProfileDto updatedProfileDto = profileService.updateProfile(accountId, requestDto);
        return ResponseEntity.ok(ApiResponse.<ProfileDto>builder()
                .code(200)
                .message("Updated profile successfully")
                .result(updatedProfileDto)
                .build());
    }

}
