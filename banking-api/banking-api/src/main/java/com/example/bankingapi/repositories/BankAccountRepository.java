package com.example.bankingapi.repositories;

import com.example.bankingapi.domain.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    BankAccount findByAccountNumber(String accountNumber);
}