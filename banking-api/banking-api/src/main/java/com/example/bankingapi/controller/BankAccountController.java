package com.example.bankingapi.controller;

import com.example.bankingapi.domain.BankAccount;
import com.example.bankingapi.services.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.bankingapi.domain.dto.BankAccountDTO;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/bank-accounts")
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    // Создание счета
    @PostMapping("/create")
    public ResponseEntity<BankAccount> createAccount(@RequestBody BankAccountDTO accountDTO) {
        BankAccount createdAccount = bankAccountService.createAccount(accountDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    // Перевод денег между счетами
    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(@RequestParam String sourceAccountNumber,
                                                @RequestParam String destinationAccountNumber,
                                                @RequestParam String pinCode,
                                                @RequestParam BigDecimal amount) {
        boolean success = bankAccountService.transferMoney(sourceAccountNumber, destinationAccountNumber, pinCode, amount);
        if (success) {
            return ResponseEntity.ok("Money transferred successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to transfer money");
        }
    }

    // Получение информации о счетах
    @GetMapping("/all")
    public ResponseEntity<List<BankAccount>> getAllAccounts() {
        List<BankAccount> accounts = bankAccountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }
}