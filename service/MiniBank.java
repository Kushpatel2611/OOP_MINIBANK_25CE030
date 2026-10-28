package service;

import static java.lang.System.out;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import model.Account;
import model.BankInfo;
import model.Command;
import model.CurrentAccount;
import model.Customer;
import model.FixedDepositAccount;
import model.SavingsAccount;
import util.AnnotationValidator;
import util.CommandParser;
import util.ReportGenerator;
import util.StatePersister;
import util.TransactionLog;
import util.Validator;

public class MiniBank {
    private static class DemoResource implements AutoCloseable {
        public void close() {
            out.println("Demo resource closed automatically.");
        }
    }

    private static class UnsafeAccount extends SavingsAccount {
        public UnsafeAccount(String ownerName, long openingBalance, long minBalance) {
            super(ownerName, openingBalance, minBalance);
        }

        @Override
        public void deposit(long amount) throws InvalidAmountException {
            if (amount <= 0) {
                throw new InvalidAmountException("Deposit amount must be greater than zero");
            }

            try {
                Field balanceField = Account.class.getDeclaredField("balance");
                balanceField.setAccessible(true);
                long currentBalance = balanceField.getLong(this);
                balanceField.setLong(this, currentBalance + amount);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Unable to update unsafe balance", e);
            }
        }
    }

    private static void runConcurrentDepositDemo(Account account, int threadCount, int times, long amount, String label)
            throws InterruptedException {
        out.println("\n--- " + label + " ---");
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(new AccountWorker(account, times, amount), "AccountWorker-" + (i + 1));
            out.println("Starting " + threads[i].getName());
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
            out.println("Joined " + thread.getName());
        }

        out.println("Expected total if all deposits succeed: " + (threadCount * times * amount));
        out.println("Final balance: " + account.getBalance());
    }

    public static void main(String[] args) throws InterruptedException {
        BankService service = new BankService();
        BankInfo bankInfo = new BankInfo("MiniBank", "Main Branch");

        if (args.length > 0 && "menu".equalsIgnoreCase(args[0])) {
            runMenu(service);
            return;
        }

        out.println("Welcome to " + bankInfo.name() + " - " + bankInfo.branch());

        out.println("\nValidator checks:");
        out.println("Valid mobile: " + Validator.isValidMobile("9876543210"));
        out.println("Invalid mobile: " + Validator.isValidMobile("1234567890"));
        out.println("Valid email: " + Validator.isValidEmail("user@example.com"));
        out.println("Invalid email: " + Validator.isValidEmail("user@com"));
        out.println("Valid PAN: " + Validator.isValidPan("ABCDE1234F"));
        out.println("Invalid PAN: " + Validator.isValidPan("ABCD1234"));
        out.println("Valid IFSC: " + Validator.isValidIfsc("ABCD0123456"));
        out.println("Invalid IFSC: " + Validator.isValidIfsc("ABCD123"));

        Command sample = CommandParser.parse("DEPOSIT AC0001 500");
        out.println("\nParsed command:");
        out.println("Type: " + sample.type());
        out.println("Account: " + sample.accountNumber());
        out.println("Amount: " + sample.amount());

        WithdrawRule anonymousRule = new WithdrawRule() {
            @Override
            public boolean allow(Account account, long amount) {
                return amount > 0 && account.getBalance() >= amount;
            }
        };

        WithdrawRule lambdaRule = (account, amount) -> amount > 0 && account.getBalance() >= amount;

        SavingsAccount demoAccount = new SavingsAccount("Kush", 1500, 500);
        out.println("\nAnonymous rule result: " + anonymousRule.allow(demoAccount, 200));
        out.println("Lambda rule result: " + lambdaRule.allow(demoAccount, 200));

        out.println("\nAccount details:");
        Account[] accounts = new Account[3];
        accounts[0] = new SavingsAccount("Kush", 1000, 500);
        accounts[1] = new CurrentAccount("Kush1", 2000, 1000);
        accounts[2] = new FixedDepositAccount("Kush3", 1500);

        for (Account account : accounts) {
            out.println(account);
            out.println("Interest Rate: " + account.interestRate() + "%");
            out.println("Yearly Interest: " + account.yearlyInterest());

            if (account instanceof SavingsAccount savings) {
                out.println("Minimum Balance: " + savings.getMinBalance());
            } else if (account instanceof CurrentAccount current) {
                out.println("Overdraft Limit: " + current.getOverdraftLimit());
            } else if (account instanceof FixedDepositAccount) {
                out.println("Premium fixed deposit: " + (account instanceof model.Premium));
            }
            out.println();
        }

        Customer customer = new Customer("Aditi", "aditi@example.com", "9876543210");
        customer.setAddress(new Customer.Address("12 Main Street", "Bangalore", "560001"));
        out.println("Customer: " + customer.getName() + " | ID: " + customer.getCustomerId());

        Account invalidAccount = new SavingsAccount("Aditi", 1500, 500);
        try {
            Field balanceField = Account.class.getDeclaredField("balance");
            balanceField.setAccessible(true);
            balanceField.setLong(invalidAccount, -50);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to inject invalid balance", e);
        }

        String[] validationErrors = AnnotationValidator.validate(invalidAccount);
        out.println("\nValidation for negative account balance:");
        for (String error : validationErrors) {
            out.println(error);
        }

        Account unsafeAccount = new UnsafeAccount("Race", 0, 0);
        runConcurrentDepositDemo(unsafeAccount, 10, 10000, 1, "Race condition with unsynchronized deposit");

        Account safeAccount = new SavingsAccount("Safe", 0, 0);
        runConcurrentDepositDemo(safeAccount, 10, 10000, 1, "Safe synchronized deposit");

        Account source = new SavingsAccount("Sam", 1000, 500);
        Account target = new CurrentAccount("Pat", 200, 1000);
        try {
            source.deposit(200);
            out.println("\nDeposit successful: " + source.getBalance());
            source.withdraw(2000);
            out.println("Withdrawal successful: " + source.getBalance());
        } catch (InvalidAmountException e) {
            out.println("Invalid amount: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            out.println("Withdrawal failed: " + e.getMessage());
            out.println("Shortfall: " + e.getShortfall());
        } finally {
            out.println("Transaction session closed.");
        }

        try {
            source.transfer(target, 5000);
            out.println("Transfer successful");
        } catch (BankException e) {
            out.println("Transfer failed: " + e.getMessage());
            if (e instanceof InsufficientFundsException insufficient) {
                out.println("Shortfall: " + insufficient.getShortfall());
            }
        } finally {
            out.println("Transfer process completed.");
        }

        out.println("\n--- Transaction processor demo ---");
        SavingsAccount txAccount = new SavingsAccount("TxUser", 500, 200);
        TransactionProcessor processor = new TransactionProcessor();
        for (int i = 0; i < 20; i++) {
            final int id = i;
            processor.submit(() -> {
                try {
                    if (id % 2 == 0) {
                        txAccount.deposit(25);
                    } else {
                        txAccount.withdraw(10);
                    }
                    out.println("Processed task " + id + ", current balance=" + txAccount.getBalance());
                } catch (InvalidAmountException | InsufficientFundsException e) {
                    out.println("Task " + id + " failed: " + e.getMessage());
                }
            });
        }
        processor.stop();
        out.println("Final balance after transaction processor: " + txAccount.getBalance());

        out.println("\n--- Producer/consumer demo ---");
        TransactionBuffer buffer = new TransactionBuffer();
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                try {
                    buffer.add("TX-" + i);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                try {
                    String tx = buffer.take();
                    out.println("Processed by consumer: " + tx);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Consumer");

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        out.println("\n--- Deadlock demo ---");
        SavingsAccount accountA = new SavingsAccount("Alice", 1000, 0);
        SavingsAccount accountB = new SavingsAccount("Bob", 1000, 0);

        Runnable deadlockTransferAtoB = () -> transferDeadlock(accountA, accountB, 50);
        Runnable deadlockTransferBtoA = () -> transferDeadlock(accountB, accountA, 50);

        Thread threadA = new Thread(deadlockTransferAtoB, "Deadlock-A");
        Thread threadB = new Thread(deadlockTransferBtoA, "Deadlock-B");
        threadA.setDaemon(true);
        threadB.setDaemon(true);
        threadA.start();
        threadB.start();
        threadA.join(1500);
        threadB.join(1500);

        if (threadA.isAlive() || threadB.isAlive()) {
            out.println("Deadlock observed with opposite lock order.");
        } else {
            out.println("Deadlock did not occur in this run.");
        }

        Runnable safeTransferAtoB = () -> transferInOrder(accountA, accountB, 25);
        Runnable safeTransferBtoA = () -> transferInOrder(accountB, accountA, 25);

        Thread safeThreadA = new Thread(safeTransferAtoB, "Safe-A");
        Thread safeThreadB = new Thread(safeTransferBtoA, "Safe-B");
        safeThreadA.start();
        safeThreadB.start();
        safeThreadA.join();
        safeThreadB.join();
        out.println("Final balances after safe transfer ordering: A=" + accountA.getBalance() + ", B="
                + accountB.getBalance());

        AccountStore accountStore = new AccountStore();
        accountStore.add(new SavingsAccount("Asha", 1200, 200));
        accountStore.add(new CurrentAccount("Ravi", 2500, 500));
        accountStore.add(new FixedDepositAccount("Meera", 3500));
        accountStore.add(new SavingsAccount("Nina", 900, 100));

        out.println("\nAccounts sorted by balance:");
        for (Account account : accountStore.sortedByBalance()) {
            out.println(account);
        }

        Repository<Account> repository = new Repository<>();
        Account first = new SavingsAccount("Asha", 1200, 200);
        Account second = new CurrentAccount("Ravi", 2500, 500);
        Account third = new FixedDepositAccount("Meera", 3500);
        repository.save(first);
        repository.save(second);
        repository.save(third);

        Account found = repository.findById(first.getAccountNumber());
        out.println("\nRepository lookup by id: " + found);
        out.println("Repository average of balances: " + Repository.average(new Integer[] { 900, 1200, 2500, 3500 }));
        out.println("All repository entries:");
        for (Account account : repository.findAll()) {
            out.println(account);
        }

        try (DemoResource resource = new DemoResource()) {
            out.println("\nUsing a resource inside try-with-resources.");
        }

        Path stateFile = Path.of("data", "accounts.dat");
        Account[] savedAccounts = new Account[] {
                new SavingsAccount("Asha", 1200, 200),
                new CurrentAccount("Ravi", 2500, 500),
                new FixedDepositAccount("Meera", 3500)
        };

        try {
            StatePersister.save(savedAccounts, stateFile);
            Account[] reloadedAccounts = StatePersister.load(stateFile);
            out.println("\nPersisted and reloaded accounts:");
            for (Account loaded : reloadedAccounts) {
                out.println(loaded);
            }
        } catch (IOException | ClassNotFoundException e) {
            out.println("Persistence failed: " + e.getMessage());
        }

        Path logsDirectory = Path.of("logs");
        Path logFile = logsDirectory.resolve("mini-bank-log.txt");
        try {
            TransactionLog.append(logFile, "DEPOSIT,250");
            TransactionLog.append(logFile, "WITHDRAW,75");
            TransactionLog.append(logFile, "DEPOSIT,100");
        } catch (IOException e) {
            out.println("Transaction logging failed: " + e.getMessage());
        }

        Path reportFile = logsDirectory.resolve("end-of-day-report.txt");
        try {
            ReportGenerator.generate(logsDirectory, reportFile);
            out.println("\nEnd-of-day report:");
            out.println(Files.readString(reportFile));
        } catch (IOException e) {
            out.println("Report generation failed: " + e.getMessage());
        }

        out.println("\nStatement for first account:");
        out.println(StatementFormatter.buildStatement(accounts[0]));

        out.println("\nComparing first account with itself: " + accounts[0].equals(accounts[0]));

        Object obj = accounts[0];
        if (obj instanceof Account account) {
            out.println("Object is an Account: " + account);
        }
    }

    private static void runMenu(BankService service) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            out.println("\nMiniBank Menu");
            out.println("1. Open Account");
            out.println("2. Deposit");
            out.println("3. Withdraw");
            out.println("4. Transfer");
            out.println("5. List Accounts");
            out.println("6. View Statement");
            out.println("7. Generate Report");
            out.println("8. Batch Transactions");
            out.println("9. Exit");
            out.print("Choose an option: ");

            String choice = scanner.nextLine();
            try {
                switch (choice.trim()) {
                    case "1" -> {
                        out.print("Owner name: ");
                        String owner = scanner.nextLine();
                        out.print("Account type (SAVINGS/CURRENT/FIXED): ");
                        String type = scanner.nextLine();
                        out.print("Opening balance: ");
                        long balance = Long.parseLong(scanner.nextLine());
                        out.print("Limit (min balance or overdraft): ");
                        long limit = Long.parseLong(scanner.nextLine());
                        Account opened = service.openAccount(owner, type, balance, limit);
                        out.println("Account opened: " + opened.getAccountNumber());
                    }
                    case "2" -> {
                        out.print("Account number: ");
                        String accountNumber = scanner.nextLine();
                        out.print("Amount: ");
                        long amount = Long.parseLong(scanner.nextLine());
                        service.deposit(accountNumber, amount);
                        out.println("Deposit successful.");
                    }
                    case "3" -> {
                        out.print("Account number: ");
                        String accountNumber = scanner.nextLine();
                        out.print("Amount: ");
                        long amount = Long.parseLong(scanner.nextLine());
                        service.withdraw(accountNumber, amount);
                        out.println("Withdrawal successful.");
                    }
                    case "4" -> {
                        out.print("From account: ");
                        String from = scanner.nextLine();
                        out.print("To account: ");
                        String to = scanner.nextLine();
                        out.print("Amount: ");
                        long amount = Long.parseLong(scanner.nextLine());
                        service.transfer(from, to, amount);
                        out.println("Transfer successful.");
                    }
                    case "5" -> {
                        List<Account> accounts = service.listAccounts();
                        for (Account account : accounts) {
                            out.println(account);
                        }
                    }
                    case "6" -> {
                        out.print("Account number: ");
                        String accountNumber = scanner.nextLine();
                        out.println(service.statement(accountNumber));
                    }
                    case "7" -> {
                        String reportPath = service.generateReport();
                        out.println("Report generated at: " + reportPath);
                    }
                    case "8" -> {
                        out.print("How many transaction tasks? ");
                        int count = Integer.parseInt(scanner.nextLine());

                        if (count <= 0) {
                            out.println("Enter a number greater than zero.");
                            break;
                        }

                        java.util.List<Runnable> tasks = new java.util.ArrayList<>();

                        for (int i = 0; i < count; i++) {
                            out.print("Account number for task " + (i + 1) + ": ");
                            String accountNumber = scanner.nextLine().trim();

                            tasks.add(() -> {
                                try {
                                    service.deposit(accountNumber, 10L);
                                    out.println("Batch deposit successful for " + accountNumber);
                                } catch (Exception e) {
                                    throw new IllegalStateException(
                                            "Transaction failed for " + accountNumber
                                                    + ": " + e.getMessage(),
                                            e);
                                }
                            });
                        }

                        service.processBatchTransactions(tasks);
                        out.println("Batch transactions completed successfully.");
                    }
                    case "9" -> {
                        service.saveAccounts();
                        running = false;
                        out.println("Goodbye.");
                    }
                    default -> out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                out.println("Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void transferDeadlock(Account first, Account second, long amount) {
        synchronized (first) {
            out.println(Thread.currentThread().getName() + " locked " + first.getAccountNumber());
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            synchronized (second) {
                try {
                    first.withdraw(amount);
                    second.deposit(amount);
                } catch (InvalidAmountException | InsufficientFundsException e) {
                    out.println("Deadlock transfer failed: " + e.getMessage());
                }
            }
        }
    }

    private static void transferInOrder(Account first, Account second, long amount) {
        Account low = first.getAccountNumber().compareTo(second.getAccountNumber()) <= 0 ? first : second;
        Account high = low == first ? second : first;

        synchronized (low) {
            synchronized (high) {
                try {
                    first.withdraw(amount);
                    second.deposit(amount);
                } catch (InvalidAmountException | InsufficientFundsException e) {
                    out.println("Ordered transfer failed: " + e.getMessage());
                }
            }
        }
    }
}
