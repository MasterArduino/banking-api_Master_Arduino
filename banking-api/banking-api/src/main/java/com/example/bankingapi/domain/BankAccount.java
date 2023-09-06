package com.example.bankingapi.domain;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String accountNumber; // Генерируется автоматически
    private String accountHolderName;
    private String pinCode;
    private BigDecimal balance;


}
