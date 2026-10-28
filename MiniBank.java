import java.util.Objects;
import java.util.Scanner;

record BankInfo(String name, String branch) {}

enum MenuOption {
    OPEN_ACCOUNT,
    DEPOSIT,
    WITHDRAW,
    TRANSFER,
    EXIT
}

public class MiniBank {
    public static void main(String[] args) {

        BankInfo bankInfo = new BankInfo("MiniBank", "Main Branch");
        System.out.println("Welcome to " + bankInfo.name() + " - " + bankInfo.branch());

        Scanner sc = new Scanner(System.in);
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
                case 1 -> "Open Account ΓÇö to be implemented in a later lab.";
                case 2 -> "Deposit ΓÇö to be implemented in a later lab.";
                case 3 -> "Withdraw ΓÇö to be implemented in a later lab.";
                case 4 -> "Transfer ΓÇö to be implemented in a later lab.";
                case 5 -> "Exiting...";
                default -> "Invalid choice. Please try again.";
            };

            System.out.println(message);

        } while (choice != 5);

        System.out.println("Thank you for using " + bankInfo.name() + ". Goodbye!");
        sc.close();

        Account[] accounts = new Account[3];
        accounts[0] = new Account("Kush", 1000);
        accounts[1] = new Account("Kush1", 2000);
        accounts[2] = new Account("Kush3", 1500);

        accounts[0].deposit(500);
        accounts[0].withdraw(200);

        accounts[1].deposit(1000);
        accounts[1].withdraw(500);

        accounts[2].deposit(250);
        accounts[2].withdraw(100);

        System.out.println("\nAccount Balances:");
        for (Account acc : accounts) {
            System.out.println(acc);
        }

        Account sameAccount = accounts[0];
        System.out.println("\nComparing first account with itself: " + accounts[0].equals(sameAccount));

        Object obj = accounts[0];
        if (obj instanceof Account) {
            System.out.println("Object is an Account: " + obj);
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

            public String getLine() { return line; }
            public String getCity() { return city; }
            public String getPincode() { return pincode; }
        }

        private static synchronized String generateCustomerId() {
            customerCounter++;
            return "CUST" + (100 + customerCounter);
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getMobile() { return mobile; }
        public String getCustomerId() { return customerId; }
        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }

        @Override
        public Customer clone() {
            try {
                Customer cloned = (Customer) super.clone();
                if (this.address != null) {
                    cloned.address = new Address(this.address.getLine(), this.address.getCity(), this.address.getPincode());
                }
                return cloned;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError("Customer clone failed", e);
            }
        }
    }

    public static class Account {
        private final String accountNumber;
        private final String ownerName;
        private long balance;
        private boolean active;
        private static long accountCounter;

        public Account(String ownerName) {
            this(ownerName, 0);
        }

        public void deposit(long amount) {
            if (amount > 0) {
                balance += amount;
            }
        }

        public boolean withdraw(long amount) {
            if (amount > 0 && balance >= amount) {
                balance -= amount;
                return true;
            }
            return false;
        }

        public Account(String ownerName, long openingBalance) {
            this.accountNumber = generateAccountNumber();
            this.ownerName = ownerName;
            this.balance = openingBalance;
            this.active = true;
        }

        private static synchronized String generateAccountNumber() {
            accountCounter++;
            return "AC" + String.format("%04d", accountCounter);
        }

        @Override
        public String toString() {
            return "Account{accountNumber='" + accountNumber + "', ownerName='" + ownerName + "', balance=" + balance + "}";
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

        public String getAccountNumber() { return accountNumber; }
        public String getOwnerName() { return ownerName; }
        public long getBalance() { return balance; }
        public boolean isActive() { return active; }
    }
}
