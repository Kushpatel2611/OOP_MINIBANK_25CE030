package service;

import exception.InvalidAmountException;
import static java.lang.System.out;
import model.Account;

public class AccountWorker implements Runnable {
    private final Account account;
    private final int times;
    private final long amount;

    public AccountWorker(Account account, int times, long amount) {
        this.account = account;
        this.times = times;
        this.amount = amount;
    }

    @Override
    public void run() {
        out.println("Thread started: " + Thread.currentThread().getName());
        for (int i = 0; i < times; i++) {
            try {
                account.deposit(amount);
            } catch (InvalidAmountException e) {
                out.println("Thread " + Thread.currentThread().getName() + " rejected amount: " + e.getMessage());
            }
        }
        out.println("Thread finished: " + Thread.currentThread().getName());
    }
}
