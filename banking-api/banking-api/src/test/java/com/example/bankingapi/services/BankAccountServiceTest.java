package com.example.bankingapi.services;
import com.example.bankingapi.domain.BankAccount;
import com.example.bankingapi.domain.dto.BankAccountDTO;
import com.example.bankingapi.repositories.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class BankAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private BankAccountService bankAccountService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateAccount() {

        BankAccountDTO accountDTO = new BankAccountDTO();
        accountDTO.setAccountHolderName("New User");
        accountDTO.setPinCode("5678");

        // Создаем мок для bankAccountRepository.save
        when(bankAccountRepository.save(any())).thenAnswer(invocation -> {
            BankAccount savedAccount = invocation.getArgument(0);
            savedAccount.setId(1L); // Устанавливаем ID для сохраненного счета
            return savedAccount;
        });

        // Вызываем метод, который создает счет
        BankAccount createdAccount = bankAccountService.createAccount(accountDTO);

        // Проверяем, что счет был успешно создан
        assertNotNull(createdAccount);
        assertEquals("New User", createdAccount.getAccountHolderName());
        assertEquals("5678", createdAccount.getPinCode());
        assertEquals(BigDecimal.ZERO, createdAccount.getBalance());
        assertNotNull(createdAccount.getAccountNumber()); // Убедимся, что номер счета не пустой
        assertEquals(1L, createdAccount.getId().longValue()); // Проверяем, что ID установлен корректно
    }

    @Test
    public void testDepositInvalidAccountOrPin() {

        String accountNumber = "123456";
        String invalidPinCode = "5678"; // Неверный PIN-код
        BankAccount testAccount = new BankAccount();
        testAccount.setAccountNumber(accountNumber);
        testAccount.setPinCode("1234"); // Допустимый PIN-код

        // Устанавливаем поведение мока для findByAccountNumber
        when(bankAccountRepository.findByAccountNumber(accountNumber)).thenReturn(testAccount);

        // Попытка сделать депозит с неверным PIN-кодом
        assertThrows(IllegalArgumentException.class,
                () -> bankAccountService.deposit(accountNumber, BigDecimal.TEN, invalidPinCode),
                "Invalid transaction");

        // Проверяем, что метод findByAccountNumber был вызван
        verify(bankAccountRepository, times(1)).findByAccountNumber(accountNumber);

        // Проверяем, что метод save не был вызван (поскольку операция была недопустимой)
        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    public void testTransferMoney() {

        String sourceAccountNumber = "sourceAccount123";
        String destinationAccountNumber = "destinationAccount456";
        String pinCode = "1234";
        BigDecimal amount = new BigDecimal("100.00");

        // Создаем моки для счета отправителя и счета получателя
        BankAccount sourceAccount = new BankAccount();
        sourceAccount.setAccountNumber(sourceAccountNumber);
        sourceAccount.setPinCode(pinCode);
        sourceAccount.setBalance(new BigDecimal("500.00"));

        BankAccount destinationAccount = new BankAccount();
        destinationAccount.setAccountNumber(destinationAccountNumber);
        destinationAccount.setBalance(new BigDecimal("200.00"));

        // Создаем моки для bankAccountRepository.findByAccountNumber
        when(bankAccountRepository.findByAccountNumber(sourceAccountNumber)).thenReturn(sourceAccount);
        when(bankAccountRepository.findByAccountNumber(destinationAccountNumber)).thenReturn(destinationAccount);

        // Вызываем метод, который выполняет перевод денег
        boolean result = bankAccountService.transferMoney(sourceAccountNumber, destinationAccountNumber, pinCode, amount);

        // Проверяем, что перевод выполнен успешно
        assertTrue(result);
        // Проверяем, что балансы счетов обновлены правильно
        assertEquals(new BigDecimal("400.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("300.00"), destinationAccount.getBalance());

        // Проверяем, что обновленные счета сохранены в репозитории
        verify(bankAccountRepository, times(1)).save(sourceAccount);
        verify(bankAccountRepository, times(1)).save(destinationAccount);
    }

    @Test
    public void testGetAllAccounts() {

        BankAccount account1 = new BankAccount();
        account1.setId(1L);
        BankAccount account2 = new BankAccount();
        account2.setId(2L);
        List<BankAccount> accounts = Arrays.asList(account1, account2);

        // Устанавливаем поведение мока для bankAccountRepository.findAll()
        when(bankAccountRepository.findAll()).thenReturn(accounts);

        // Вызываем метод getAllAccounts
        List<BankAccount> result = bankAccountService.getAllAccounts();

        // Проверяем, что результат совпадает с ожидаемым списком счетов
        assertEquals(accounts, result);
    }
    @Test
    public void testWithdrawWithSufficientBalance() {

        String accountNumber = "12345";
        String pinCode = "5678";
        BigDecimal initialBalance = new BigDecimal("100.00"); // Начальный баланс

        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(accountNumber);
        bankAccount.setPinCode(pinCode);
        bankAccount.setBalance(initialBalance);

        // Устанавливаем поведение мока для bankAccountRepository.findByAccountNumber()
        when(bankAccountRepository.findByAccountNumber(accountNumber)).thenReturn(bankAccount);

        // Выполняем снятие средств с достаточным балансом
        BigDecimal amountToWithdraw = new BigDecimal("50.00");
        boolean result = bankAccountService.withdraw(accountNumber, pinCode, amountToWithdraw);

        // Проверяем, что снятие было успешным
        assertTrue(result);

        // Проверяем, что баланс уменьшился на сумму снятия
        assertEquals(initialBalance.subtract(amountToWithdraw), bankAccount.getBalance());
    }
    @Test
    public void testGenerateAccountNumber() {
        BankAccountService bankAccountService = new BankAccountService();

        String accountNumber = bankAccountService.generateAccountNumber();

        assertNotNull(accountNumber);
        assertTrue(accountNumber.matches("[a-f0-9-]+")); // Проверяем, что номер имеет правильный формат UUID
    }
}

