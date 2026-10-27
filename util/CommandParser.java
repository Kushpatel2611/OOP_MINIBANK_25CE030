package util;

import model.Command;
import model.TransactionType;

public final class CommandParser {
    private CommandParser() {
    }

    public static Command parse(String line) {
        String[] parts = line.trim().split("\\s+");

        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Command must be in format: TYPE ACCOUNT AMOUNT");
        }

        TransactionType type =
                TransactionType.valueOf(parts[0].toUpperCase());
        String accountNumber = parts[1];
        long amount = Long.parseLong(parts[2]);

        return new Command(type, accountNumber, amount);
    }
}
