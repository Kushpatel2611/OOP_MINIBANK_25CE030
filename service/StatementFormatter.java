package service;

import model.Account;

public final class StatementFormatter {
    private StatementFormatter() {
    }

    public static String buildStatement(Account account) {
        StringBuilder sb = new StringBuilder();

        sb.append("Account Statement\n");
        sb.append("-----------------\n");
        sb.append("Account Number: ")
                .append(account.getAccountNumber()).append("\n");
        sb.append("Owner Name: ")
                .append(account.getOwnerName()).append("\n");
        sb.append("Balance: ")
                .append(account.getBalance()).append("\n");
        sb.append("Interest Rate: ")
                .append(account.interestRate()).append("%\n");
        sb.append("Status: ")
                .append(account.isActive() ? "Active" : "Inactive");

        return sb.toString();
    }
}
