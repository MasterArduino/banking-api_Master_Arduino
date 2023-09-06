package com.example.bankingapi.services;

import com.example.bankingapi.domain.BankAccount;
import com.example.bankingapi.domain.dto.BankAccountDTO;
import com.example.bankingapi.repositories.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class BankAccountService {
    @Autowired
    private BankAccountRepository bankAccountRepository;

//     Создаем новый банковский счет на основе переданных данных из объекта BankAccountDTO.
    public BankAccount createAccount(BankAccountDTO accountDTO) {
        // Генерация случайного номера счета
        String accountNumber = generateAccountNumber();
        // Создание нового счета с заданными данными
        BankAccount newAccount = createNewAccount(accountNumber, accountDTO.getAccountHolderName(), accountDTO.getPinCode());
        // Сохранение счета в репозитории
        return bankAccountRepository.save(newAccount);
    }

//     Генерируем и возвращает случайный номер банковского счета в виде строки.
    public String generateAccountNumber() {

        return UUID.randomUUID().toString();
    }

//    Осуществляем депозит на счет с заданным номером.
    public BankAccount deposit(String accountNumber, BigDecimal amount, String pinCode) {
        // Поиск счета по номеру
        BankAccount bankAccount = findAccountByAccountNumber(accountNumber);
        // Проверка существования счета и подтверждения операции с помощью pinCode
        validateAccountAndPinCode(bankAccount, pinCode);

        // Увеличение баланса на счету с учетом точности двух знаков после запятой
        BigDecimal newBalance = bankAccount.getBalance().add(amount).setScale(2, RoundingMode.HALF_UP);
        bankAccount.setBalance(newBalance);

        // Сохранение обновленного счета в репозитории
        bankAccountRepository.save(bankAccount);

        // Возвращение обновленного счета
        return bankAccount;
    }

//     Осуществляем снятие средств с банковского счета.
    public boolean withdraw(String accountNumber, String pinCode, BigDecimal amount) {
        // Поиск счета по номеру
        BankAccount bankAccount = findAccountByAccountNumber(accountNumber);
        // Проверка существования счета и подтверждения операции с помощью pinCode
        validateAccountAndPinCode(bankAccount, pinCode);

        // Проверка наличия достаточных средств на счете
        if (!hasSufficientFunds(bankAccount, amount)) {
            return false; // Ошибка
        }

        // Уменьшение баланса на счету
        BigDecimal newBalance = bankAccount.getBalance().subtract(amount);
        bankAccount.setBalance(newBalance);

        // Сохранение обновленного счета в репозитории
        bankAccountRepository.save(bankAccount);

        return true; // Успешное снятие
    }

//      Осуществляем перевод средств между двумя банковскими счетами.
    public boolean transferMoney(String sourceAccountNumber, String destinationAccountNumber, String pinCode, BigDecimal amount) {
        // Поиск счета отправителя и получателя по номерам
        BankAccount sourceAccount = findAccountByAccountNumber(sourceAccountNumber);
        BankAccount destinationAccount = findAccountByAccountNumber(destinationAccountNumber);

        // Проверка существования счетов и подтверждения операции с помощью pinCode
        validateAccountAndPinCode(sourceAccount, pinCode);

        // Проверка наличия средств на счете отправителя
        if (destinationAccount == null || !hasSufficientFunds(sourceAccount, amount)) {
            return false; // Ошибка
        }

        // Выполнение перевода
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));

        bankAccountRepository.save(sourceAccount);
        bankAccountRepository.save(destinationAccount);

        return true; // Успешный перевод
    }

//      Создаем новый объект BankAccount с заданными параметрами и возвращает его.

    protected BankAccount createNewAccount(String accountNumber, String accountHolderName, String pinCode) {
        BankAccount newAccount = new BankAccount();
        newAccount.setAccountNumber(accountNumber);
        newAccount.setAccountHolderName(accountHolderName);
        newAccount.setPinCode(pinCode);
        newAccount.setBalance(BigDecimal.ZERO);
        return newAccount;
    }
//    Ищет банковский счет по его номеру и возвращает его.
    protected BankAccount findAccountByAccountNumber(String accountNumber) {
        return bankAccountRepository.findByAccountNumber(accountNumber);
    }

//      Проверяем, действителен ли банковский счет и соответствует ли указанный пин-код.
//      Если счет не действителен или пин-код не совпадает, генерируется исключение IllegalArgumentException.
    protected void validateAccountAndPinCode(BankAccount account, String pinCode) {
        if (account == null || !account.getPinCode().equals(pinCode)) {
            throw new IllegalArgumentException("Invalid transaction");
        }
    }

//    Проверяем, достаточно ли средств на банковском счете для выполнения операции.
    protected boolean hasSufficientFunds(BankAccount account, BigDecimal amount) {
        return account.getBalance().compareTo(amount) >= 0;
    }
    public List<BankAccount> getAllAccounts() {
        return bankAccountRepository.findAll();
    }
}