package model;

import exception.AccountNotFoundException;
import exception.BankException;
import exception.InsufficientFundsException;
import exception.InvalidAmountException;
import java.io.Serializable;
import java.util.Objects;
import model.annotation.Id;
import model.annotation.Positive;

public abstract class Account implements Transactable, InterestBearing, Premium, Comparable<Account>, Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private final String accountNumber;
    private final String ownerName;
    @Positive
    private long balance;
    private boolean active;

    private static long accountCounter;

    public Account(String ownerName) {
        this(ownerName, 0);
    }

    public Account(String ownerName, long openingBalance) {
        if (openingBalance < 0) {
            throw new IllegalArgumentException(
                    "Opening balance cannot be negative");
        }

        this.accountNumber = generateAccountNumber();
        this.ownerName = ownerName;
        this.balance = openingBalance;
        this.active = true;
    }

    @Override
    public synchronized void deposit(long amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero");
        }
        balance += amount;
    }

    @Override
    public synchronized void withdraw(long amount) throws InsufficientFundsException, InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero");
        }

        if (!canWithdraw(amount)) {
            long shortfall = amount - balance;
            throw new InsufficientFundsException("Insufficient funds for withdrawal", shortfall);
        }

        balance -= amount;
    }

    public void transfer(Account to, long amount) throws BankException {
        try {
            if (to == null) {
                throw new AccountNotFoundException("Destination account not found");
            }
            this.withdraw(amount);
            to.deposit(amount);
        } catch (BankException e) {
            throw e;
        } catch (Exception e) {
            throw new BankException("Transfer failed: " + e.getMessage(), e);
        } finally {
            System.out.println("Transfer attempt finished for " + this.accountNumber + " -> "
                    + (to != null ? to.accountNumber : "N/A"));
        }
    }

    @Override
    public abstract double interestRate();

    public abstract boolean canWithdraw(long amount);

    public static synchronized void syncAccountCounterFromAccounts(Account[] accounts) {
        if (accounts == null || accounts.length == 0) {
            return;
        }

        long maxNumber = 0L;
        for (Account account : accounts) {
            if (account == null || account.accountNumber == null) {
                continue;
            }

            String number = account.accountNumber;
            if (number.startsWith("AC")) {
                try {
                    long parsed = Long.parseLong(number.substring(2));
                    if (parsed > maxNumber) {
                        maxNumber = parsed;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        accountCounter = Math.max(accountCounter, maxNumber);
    }

    private static synchronized String generateAccountNumber() {
        accountCounter++;
        return "AC" + String.format("%04d", accountCounter);
    }

    @Override
    public String toString() {
        return "Account{accountNumber='" + accountNumber
                + "', ownerName='" + ownerName
                + "', balance=" + balance + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account account)) return false;
        return accountNumber.equals(account.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountNumber);
    }

    @Override
    public int compareTo(Account other) {
        return this.accountNumber.compareTo(other.accountNumber);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    @Override
    public synchronized long getBalance() {
        return balance;
    }

    public boolean isActive() {
        return active;
    }
}
