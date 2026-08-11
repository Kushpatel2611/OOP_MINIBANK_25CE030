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
                case 1 -> "Open Account — to be implemented in a later lab.";
                case 2 -> "Deposit — to be implemented in a later lab.";
                case 3 -> "Withdraw — to be implemented in a later lab.";
                case 4 -> "Transfer — to be implemented in a later lab.";
                case 5 -> "Exiting...";
                default -> "Invalid choice. Please try again.";
            };
            
            System.out.println(message);

        } while (choice != 5);

        System.out.println("Thank you for using " + bankInfo.name() + ". Goodbye!");
        sc.close();
    }
}
