package service;

import exception.AccountNotFoundException;
import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import model.Account;
import model.CurrentAccount;
import model.FixedDepositAccount;
import model.SavingsAccount;
import util.ReportGenerator;
import util.StatePersister;
import util.Validator;

public class BankService {
    private final Repository<Account> repository = new Repository<>();
    private final TransactionProcessor processor = new TransactionProcessor();
    private final Path persistenceFile = Path.of("data", "accounts.dat");
    private final Path reportFile = Path.of("logs", "mini-bank-report.txt");

    public BankService() {
        loadAccounts();
    }

    public Account openAccount(String ownerName, String accountType, long openingBalance, long limit) throws BankException {
        if (!Validator.isValidName(ownerName)) {
            throw new IllegalArgumentException("Owner name is required.");
        }

        if (!Validator.isValidAccountType(accountType)) {
            throw new IllegalArgumentException("Account type must be SAVINGS, CURRENT, or FIXED.");
        }

        if (openingBalance < 0) {
            throw new IllegalArgumentException("Opening balance cannot be negative.");
        }

        Account account;
        String normalizedType = accountType.trim().toUpperCase();
        switch (normalizedType) {
            case "SAVINGS" -> account = new SavingsAccount(ownerName, openingBalance, limit);
            case "CURRENT" -> account = new CurrentAccount(ownerName, openingBalance, limit);
            case "FIXED", "FIXED_DEPOSIT" -> account = new FixedDepositAccount(ownerName, openingBalance);
            default -> throw new IllegalArgumentException("Unsupported account type: " + accountType);
        }

        repository.save(account);
        processor.addAccount(account);
        return account;
    }

    public Account getAccount(String accountNumber) throws AccountNotFoundException {
        Account account = repository.findById(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountNumber);
        }
        return account;
    }

    public void deposit(String accountNumber, long amount) throws AccountNotFoundException, InvalidAmountException {
        Account account = getAccount(accountNumber);
        if (!Validator.isPositiveAmount(amount)) {
            throw new InvalidAmountException("Deposit amount must be greater than zero");
        }
        account.deposit(amount);
    }

    public void withdraw(String accountNumber, long amount) throws AccountNotFoundException, InvalidAmountException, InsufficientFundsException {
        Account account = getAccount(accountNumber);
        if (!Validator.isPositiveAmount(amount)) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero");
        }
        account.withdraw(amount);
    }

    public void transfer(String fromAccountNumber, String toAccountNumber, long amount)
            throws AccountNotFoundException, BankException {
        Account fromAccount = getAccount(fromAccountNumber);
        Account toAccount = getAccount(toAccountNumber);
        if (fromAccount == null || toAccount == null) {
            throw new AccountNotFoundException("Source or destination account not found");
        }
        fromAccount.transfer(toAccount, amount);
    }

    public String statement(String accountNumber) throws AccountNotFoundException {
        Account account = getAccount(accountNumber);
        return service.StatementFormatter.buildStatement(account)
                + "\nTransaction history: " + processor.getAccount(accountNumber);
    }

    public List<Account> listAccounts() {
        return repository.findAll();
    }

    public void saveAccounts() {
        try {
            Account[] accounts = repository.findAll().toArray(new Account[0]);
            StatePersister.save(accounts, persistenceFile);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save accounts", e);
        }
    }

    public void loadAccounts() {
        try {
            Account[] accounts = StatePersister.load(persistenceFile);
            Account.syncAccountCounterFromAccounts(accounts);
            for (Account account : accounts) {
                repository.save(account);
                processor.addAccount(account);
            }
        } catch (IOException | ClassNotFoundException e) {
            // First run or no saved data yet; ignore and continue.
        }
    }

    public String generateReport() throws IOException {
        Path logsDirectory = Path.of("logs");
        Path report = logsDirectory.resolve("mini-bank-report.txt");
        ReportGenerator.generate(logsDirectory, report);
        return report.toString();
    }

    public void processBatchTransactions(List<Runnable> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        List<Future<?>> futures = new ArrayList<>();
        for (Runnable task : tasks) {
            futures.add(processor.submitTask(task));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Batch transaction interrupted", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Batch transaction failed", e.getCause());
            }
        }
    }

    public TransactionProcessor getProcessor() {
        return processor;
    }
}
