package com.chillguy.tiny.blood.repository;

import com.chillguy.tiny.blood.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    boolean existsByUserNameIgnoreCase(String userName);

    boolean existsByEmail(String email);

    Optional<Account> findByUserName(String userName);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByAccountId(String accountId);

    Optional<Account> findByUserNameIgnoreCase(String username);
}
