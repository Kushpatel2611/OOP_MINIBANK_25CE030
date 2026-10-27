package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import exception.InsufficientFundsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import model.Account;
import org.junit.jupiter.api.Test;

public class BankServiceTest {
    @Test
    void deposit_increases_balance() throws Exception {
        BankService service = new BankService();
        Account account = service.openAccount("Alice", "SAVINGS", 1000L, 500L);

        service.deposit(account.getAccountNumber(), 250L);

        assertEquals(1250L, service.getAccount(account.getAccountNumber()).getBalance());
    }

    @Test
    void withdrawing_beyond_the_allowed_limit_throws_expected_exception() throws Exception {
        BankService service = new BankService();
        Account account = service.openAccount("Bob", "SAVINGS", 1000L, 500L);

        InsufficientFundsException exception = assertThrows(
                InsufficientFundsException.class,
                () -> service.withdraw(account.getAccountNumber(), 2001L));

        assertTrue(exception.getShortfall() > 0);
    }

    @Test
    void multiple_concurrent_deposits_produce_the_correct_final_balance() throws Exception {
        BankService service = new BankService();
        Account account = service.openAccount("Charlie", "SAVINGS", 0L, 0L);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            tasks.add(() -> {
                service.deposit(account.getAccountNumber(), 25L);
                return null;
            });
        }

        try {
            List<Future<Void>> futures = executor.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdown();
        }

        assertEquals(250L, service.getAccount(account.getAccountNumber()).getBalance());
    }

    @Test
    void saving_and_reloading_accounts_keeps_ids_unique_and_allows_new_account() throws Exception {
        Path dataFile = Path.of("data", "accounts.dat");
        Files.createDirectories(dataFile.getParent());
        Files.deleteIfExists(dataFile);

        BankService service = new BankService();
        Account initial = service.openAccount("Dana", "SAVINGS", 1000L, 500L);
        service.saveAccounts();

        BankService reloaded = new BankService();
        Account reloadedAccount = reloaded.getAccount(initial.getAccountNumber());
        assertEquals(initial.getAccountNumber(), reloadedAccount.getAccountNumber());

        Account nextAccount = reloaded.openAccount("Evan", "CURRENT", 300L, 1000L);
        assertNotEquals(initial.getAccountNumber(), nextAccount.getAccountNumber());
        assertEquals(2, reloaded.listAccounts().size());
    }
}
