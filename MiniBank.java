import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;

record BankInfo(String name, String branch) {}

enum MenuOption {
    OPEN_ACCOUNT,
    DEPOSIT,
    WITHDRAW,
    TRANSFER,
    EXIT
}

enum TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER
}

record Command(TransactionType type, String accountNumber, long amount) {}

class Validator {
    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PAN_PATTERN =
            Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]$");
    private static final Pattern IFSC_PATTERN =
            Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");

    public static boolean isValidMobile(String mobile) {
        return mobile != null && MOBILE_PATTERN.matcher(mobile).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPan(String pan) {
        return pan != null && PAN_PATTERN.matcher(pan).matches();
    }

    public static boolean isValidIfsc(String ifsc) {
        return ifsc != null && IFSC_PATTERN.matcher(ifsc).matches();
    }
}

class CommandParser {
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

class StatementFormatter {
    public static String buildStatement(MiniBank.Account account) {
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

public class MiniBank {

    public static void main(String[] args) {

        BankInfo bankInfo = new BankInfo("MiniBank", "Main Branch");

        System.out.println("Welcome to " + bankInfo.name()
                + " - " + bankInfo.branch());

        // Validator tests
        System.out.println("\nValidator checks:");
        System.out.println("Valid mobile: "
                + Validator.isValidMobile("9876543210"));
        System.out.println("Invalid mobile: "
                + Validator.isValidMobile("1234567890"));

        System.out.println("Valid email: "
                + Validator.isValidEmail("user@example.com"));
        System.out.println("Invalid email: "
                + Validator.isValidEmail("user@com"));

        System.out.println("Valid PAN: "
                + Validator.isValidPan("ABCDE1234F"));
        System.out.println("Invalid PAN: "
                + Validator.isValidPan("ABCD1234"));

        System.out.println("Valid IFSC: "
                + Validator.isValidIfsc("ABCD0123456"));
        System.out.println("Invalid IFSC: "
                + Validator.isValidIfsc("ABCD123"));

        // Command parser test
        Command sample = CommandParser.parse("DEPOSIT AC0001 500");

        System.out.println("\nParsed command:");
        System.out.println("Type: " + sample.type());
        System.out.println("Account: " + sample.accountNumber());
        System.out.println("Amount: " + sample.amount());

        // Menu
        try (Scanner sc = new Scanner(System.in)) {
            int choice;

            do {
                System.out.println("\nMenu:");
                System.out.println("1. Open Account");
                System.out.println("2. Deposit");
                System.out.println("3. Withdraw");
                System.out.println("4. Transfer");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");

                choice = sc.nextInt();

                String message = switch (choice) {
                    case 1 -> "Open Account — to be implemented in a later lab.";
                    case 2 -> "Deposit — to be implemented in a later lab.";
                    case 3 -> "Withdraw — to be implemented in a later lab.";
                    case 4 -> "Transfer — to be implemented in a later lab.";
                    case 5 -> "Exiting...";
                    default -> "Invalid choice. Please try again.";
                };

                System.out.println(message);

            } while (choice != 5);
        }

        System.out.println("Thank you for using "
                + bankInfo.name() + ". Goodbye!");

        // Runtime polymorphism: three different account types
        Account[] accounts = new Account[3];

        accounts[0] = new SavingsAccount("Kush", 1000, 500);
        accounts[1] = new CurrentAccount("Kush1", 2000, 1000);
        accounts[2] = new FixedDepositAccount("Kush3", 1500);

        // Deposit and withdrawal tests
        accounts[0].deposit(500);
        accounts[0].withdraw(200);

        accounts[1].deposit(1000);
        accounts[1].withdraw(500);

        accounts[2].deposit(250);
        boolean fdWithdrawal = accounts[2].withdraw(100);

        System.out.println("\nFixed deposit withdrawal successful: "
                + fdWithdrawal);

        // Print accounts and demonstrate polymorphism
        System.out.println("\nAccount Details:");

        for (Account acc : accounts) {
            System.out.println(acc);
            System.out.println("Interest Rate: "
                    + acc.interestRate() + "%");

            // Pattern matching with instanceof
            if (acc instanceof SavingsAccount savings) {
                System.out.println("Minimum Balance: "
                        + savings.getMinBalance());
            } else if (acc instanceof CurrentAccount current) {
                System.out.println("Overdraft Limit: "
                        + current.getOverdraftLimit());
            } else if (acc instanceof FixedDepositAccount) {
                System.out.println("Account Type: Fixed Deposit");
            }

            System.out.println();
        }

        // Account statement
        System.out.println("Statement for first account:");
        System.out.println(StatementFormatter.buildStatement(accounts[0]));

        // equals() demonstration
        Account sameAccount = accounts[0];

        System.out.println("\nComparing first account with itself: "
                + accounts[0].equals(sameAccount));

        // instanceof demonstration
        Object obj = accounts[0];

        if (obj instanceof Account account) {
            System.out.println("Object is an Account: " + account);
        } else {
            System.out.println("Object is not an Account.");
        }
    }

    public static class Customer implements Cloneable {
        private final String name;
        private final String email;
        private final String mobile;
        private final String customerId;
        private Address address;

        private static long customerCounter;

        public Customer(String name, String email, String mobile) {
            this.name = name;
            this.email = email;
            this.mobile = mobile;
            this.customerId = generateCustomerId();
        }

        public static class Address {
            private final String line;
            private final String city;
            private final String pincode;

            public Address(String line, String city, String pincode) {
                this.line = line;
                this.city = city;
                this.pincode = pincode;
            }

            public String getLine() {
                return line;
            }

            public String getCity() {
                return city;
            }

            public String getPincode() {
                return pincode;
            }
        }

        private static synchronized String generateCustomerId() {
            customerCounter++;
            return "CUST" + (100 + customerCounter);
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getMobile() {
            return mobile;
        }

        public String getCustomerId() {
            return customerId;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        @Override
        public Customer clone() {
            try {
                Customer cloned = (Customer) super.clone();

                if (this.address != null) {
                    cloned.address = new Address(
                            this.address.getLine(),
                            this.address.getCity(),
                            this.address.getPincode());
                }

                return cloned;

            } catch (CloneNotSupportedException e) {
                throw new AssertionError("Customer clone failed", e);
            }
        }
    }

    public abstract static class Account {
        private final String accountNumber;
        private final String ownerName;
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

        public void deposit(long amount) {
            if (amount > 0) {
                balance += amount;
            }
        }

        public boolean withdraw(long amount) {
            if (amount > 0 && canWithdraw(amount)) {
                balance -= amount;
                return true;
            }

            return false;
        }

        public abstract double interestRate();

        public abstract boolean canWithdraw(long amount);

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
            if (!(o instanceof Account)) return false;

            Account account = (Account) o;
            return accountNumber.equals(account.accountNumber);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountNumber);
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public String getOwnerName() {
            return ownerName;
        }

        public long getBalance() {
            return balance;
        }

        public boolean isActive() {
            return active;
        }
    }

    public static class SavingsAccount extends Account {
        private final long minBalance;

        public SavingsAccount(String ownerName, long openingBalance,
                              long minBalance) {
            super(ownerName, openingBalance);

            if (minBalance < 0 || openingBalance < minBalance) {
                throw new IllegalArgumentException(
                        "Opening balance must be at least the minimum balance");
            }

            this.minBalance = minBalance;
        }

        @Override
        public double interestRate() {
            return 4.0;
        }

        @Override
        public boolean canWithdraw(long amount) {
            return amount > 0 && getBalance() - amount >= minBalance;
        }

        public long getMinBalance() {
            return minBalance;
        }
    }

    public static class CurrentAccount extends Account {
        private final long overdraftLimit;

        public CurrentAccount(String ownerName, long openingBalance,
                              long overdraftLimit) {
            super(ownerName, openingBalance);

            if (overdraftLimit < 0) {
                throw new IllegalArgumentException(
                        "Overdraft limit cannot be negative");
            }

            this.overdraftLimit = overdraftLimit;
        }

        @Override
        public double interestRate() {
            return 0.0;
        }

        @Override
        public boolean canWithdraw(long amount) {
            return amount > 0
                    && getBalance() - amount >= -overdraftLimit;
        }

        public long getOverdraftLimit() {
            return overdraftLimit;
        }
    }

    public static class FixedDepositAccount extends Account {
        public FixedDepositAccount(String ownerName, long openingBalance) {
            super(ownerName, openingBalance);
        }

        @Override
        public double interestRate() {
            return 7.0;
        }

        @Override
        public boolean canWithdraw(long amount) {
            return false;
        }
    }
}