package service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import model.Account;

public class TransactionProcessor {
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final ConcurrentMap<String, Account> sharedAccounts = new ConcurrentHashMap<>();

    public void addAccount(Account account) {
        if (account != null) {
            sharedAccounts.put(account.getAccountNumber(), account);
        }
    }

    public Account getAccount(String accountNumber) {
        return sharedAccounts.get(accountNumber);
    }

    public Map<String, Account> getSharedAccounts() {
        return sharedAccounts;
    }

    public void submit(Runnable task) {
        executorService.execute(task);
    }

    public Future<?> submitTask(Runnable task) {
        return executorService.submit(task);
    }

    public void stop() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }
}
