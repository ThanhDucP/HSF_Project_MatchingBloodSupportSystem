package com.chillguy.tiny.blood.service;

import com.chillguy.tiny.blood.entity.Account;
import com.chillguy.tiny.blood.repository.AccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String accountId) throws UsernameNotFoundException {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy account ID"));

        return new org.springframework.security.core.userdetails.User(
                account.getUserName(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().getRole()))
        );
    }



}

