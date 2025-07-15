package com.chillguy.tiny.blood.service;

import com.chillguy.tiny.blood.dto.ProfileUpdateDto;
import com.chillguy.tiny.blood.dto.response.ProfileDto;
import com.chillguy.tiny.blood.entity.Account;
import com.chillguy.tiny.blood.entity.Profile;
import com.chillguy.tiny.blood.exception.AccountNotFoundException;
import com.chillguy.tiny.blood.exception.ProfileIsExistedException;
import com.chillguy.tiny.blood.exception.ProfileNotFoundException;
import com.chillguy.tiny.blood.mapper.AddressMapper;
import com.chillguy.tiny.blood.mapper.ProfileMapper;
import com.chillguy.tiny.blood.repository.AccountRepository;
import com.chillguy.tiny.blood.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ProfileServiceImpl implements IProfileService {

    private final ProfileRepository profileRepository;

    private final AccountRepository accountRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository, AccountRepository accountRepository) {
        this.profileRepository = profileRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public ProfileDto createProfile(String accountId, ProfileDto profileUpdateDto) {

        Optional<Account> accountInDb = accountRepository.findByAccountId(accountId);

        if (accountInDb.isEmpty())
            throw new AccountNotFoundException("Account not found with Id: " + accountId);

        Account account = accountInDb.get();

        if (account.getProfile() != null)
            throw new ProfileIsExistedException("Profile already existed.");

        Profile profile = new Profile();

        profile.setProfileId(generateProfileId());
        profile.setAccount(account);
        profile.setName(profileUpdateDto.getName());
        profile.setPhone(profileUpdateDto.getPhone());
        profile.setDob(profileUpdateDto.getDob());
        profile.setGender(profileUpdateDto.getGender());
        profile.setAddress(AddressMapper.addressMapper(profileUpdateDto.getAddress()));

        Profile updatedProfile = profileRepository.save(profile);
        account.setProfile(updatedProfile);

        ProfileDto updatedProfileDto = ProfileMapper.profileMapper(updatedProfile);
        return updatedProfileDto;
    }

    @Override
    public ProfileDto getProfileByAccountId(String accountId) {

        Optional<Account> accountInDb = accountRepository.findByAccountId(accountId);
        if (accountInDb.isEmpty())
            throw new AccountNotFoundException("Account not found with Id: " + accountId);

        if (accountInDb.get().getProfile() == null)
            throw new ProfileNotFoundException(String.format("Profile of the account: %s hasn't been created yet", accountId));

        Profile profileInDb = accountInDb.get().getProfile();
        ProfileDto responseDto = ProfileMapper.profileMapper(profileInDb);

        return responseDto;
    }

    @Override
    public ProfileDto updateProfile(String accountId, ProfileUpdateDto requestDto) {

        Optional<Account> accountInDb = accountRepository.findByAccountId(accountId);

        if (accountInDb.isEmpty())
            throw new AccountNotFoundException("Account not found with Id: " + accountId);

        Profile profileInDb = accountInDb.get().getProfile();

        if (profileInDb == null)
            throw new ProfileNotFoundException(String.format("Profile of the account: %s hasn't been created", accountId));
        profileInDb.setName(requestDto.getName());
        profileInDb.setPhone(requestDto.getPhone());
        profileInDb.setDob(requestDto.getDob());
        profileInDb.setAddress(AddressMapper.addressMapper(requestDto.getAddress()));
        profileInDb.setGender(requestDto.getGender());

        profileRepository.save(profileInDb);

        ProfileDto responseDto = ProfileMapper.profileMapper(profileInDb);
        return responseDto;
    }

    private String generateProfileId() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(0, 1000); // 0 to 999
        String randomPart = String.format("%03d", random); // zero-padded to 3 digits
        return "PR-" + datePart + "-" + randomPart;
    }
}
