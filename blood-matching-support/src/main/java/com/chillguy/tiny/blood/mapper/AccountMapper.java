package com.chillguy.tiny.blood.mapper;

import com.chillguy.tiny.blood.dto.RegisterResponseDto;
import com.chillguy.tiny.blood.entity.Account;

public class AccountMapper {

    public static RegisterResponseDto accountMapper(Account newAccount) {
        if (newAccount == null) return null;
        RegisterResponseDto registerResponseDto = new RegisterResponseDto();
        registerResponseDto.setAccountId(newAccount.getAccountId());
        registerResponseDto.setRole(newAccount.getRole().getRole());
        registerResponseDto.setUsername(newAccount.getUserName());
        return registerResponseDto;
    }
}
