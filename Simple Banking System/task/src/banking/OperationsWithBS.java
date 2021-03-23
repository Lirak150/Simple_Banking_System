package banking;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

class OperationsWithBS {
    private final Scanner scanner = new Scanner(System.in);

    private final BankingSystem bs;
    private Account currentAccount;

    private static final String MAIN_MENU = "1. Create an account\n" +
            "2. Log into account\n" +
            "0. Exit\n" +
            ">";
    private static final String ACCOUNT_MENU = "1. Balance\n" +
            "2. Add income\n" +
            "3. Do transfer\n" +
            "4. Close account\n" +
            "5. Log out\n" +
            "0. Exit\n" +
            ">";
    private static final String BYE = "Bye!";
    private static final String CLOSE_ACCOUNT = "The account has been closed!\n";
    private static final String SUC_LOG_OUT = "You have successfully logged out!\n";
    private static final String SUC_LOG_IN = "You have successfully logged in!\n";
    private static final String UNSUC_LOG_IN = "Wrong card number or PIN!\n";
    private static final String ERROR = "Error!\n";
    private static final String INCOME_ADDED = "Income was added!\n";
    private static final String SUCCESS = "Success!\n";
    private static final String ENTER_INCOME = "Enter income:\n" +
            ">";
    private static final String TRANSFER = "Transfer\n" +
            "Enter card number:\n" +
            ">";
    private static final String CREATION =
            "Your card has been created\n" +
                    "Your card number:\n" +
                    "%s\n" +
                    "Your card PIN:\n" +
                    "%s\n";
    private static final String ENTER_CARD = "Enter your card number:\n" +
            ">";
    private static final String ENTER_PIN = "Enter your PIN:\n" +
            ">";
    private static final String BALANCE = "Balance: %d\n";
    private static final String MONEY_TO_TRANSFER = "Enter how much money you want to transfer:\n" +
            ">";

    public OperationsWithBS(BankingSystem bs) {
        this.bs = bs;
    }

    public void start() {
        processMainMenu();
    }

    private void processMainMenu() {
        while (currentAccount == null) {
            System.out.print(MAIN_MENU);
            int command = scanner.nextInt();
            if (command == 0) {
                bye();
            } else if (command == 1) {
                createAnAccount();
            } else if (command == 2) {
                logIn();
            } else {
                System.out.println("Unsupported operation");
            }
        }
        processAccountMenu();
    }

    private void createAnAccount() {
        Account account = bs.addAccount();
        String idStr = account.getCard();
        String pinStr = account.getPin();
        System.out.printf(CREATION, idStr, pinStr);
        System.out.println();
    }

    private void logIn() {
        System.out.print(ENTER_CARD);
        String card = scanner.next();
        System.out.print(ENTER_PIN);
        String pinStr = scanner.next();

        Optional<Account> opAccount = bs.getAccount(card, pinStr);
        if (opAccount.isEmpty()) {
            System.out.println(UNSUC_LOG_IN);
        } else {
            System.out.println(SUC_LOG_IN);
            currentAccount = opAccount.get();
        }
    }

    private void processAccountMenu() {
        while (currentAccount != null) {
            System.out.print(ACCOUNT_MENU);
            int command = scanner.nextInt();
            switch (command) {
                case 0:
                    bye();
                    break;
                case 1:
                    showBalance();
                    break;
                case 2:
                    addIncome();
                    break;
                case 3:
                    doTransfer();
                    break;
                case 4:
                    closeAccount();
                    break;
                case 5:
                    logOut();
                    break;
                default:
                    System.out.println("Unsupported operation");
            }
        }
        processMainMenu();
    }

    private void doTransfer() {
        System.out.print(TRANSFER);
        String cardToTransfer = scanner.next();
        List<String> messages = bs.tryOperation(currentAccount, cardToTransfer);
        if (!messages.isEmpty()) {
            for (String message : messages) {
                System.out.println(message);
            }
            return;
        }

        System.out.print(MONEY_TO_TRANSFER);
        int deposit = scanner.nextInt();
        Optional<String> checkDeposit = bs.checkDeposit(currentAccount, deposit);
        if (checkDeposit.isPresent()) {
            System.out.println(checkDeposit.get());
            return;
        }

        boolean isSuccess = bs.makeOperation(deposit, currentAccount, cardToTransfer);

        if (isSuccess) {
            System.out.println(SUCCESS);
        } else {
            System.out.println(ERROR);
        }
    }

    private void addIncome() {
        System.out.print(ENTER_INCOME);
        int deposit = scanner.nextInt();
        bs.addIncome(deposit, currentAccount);
        currentAccount.setBalance(currentAccount.getBalance() + deposit);
        System.out.println(INCOME_ADDED);
    }

    private void closeAccount() {
        bs.closeAccount(currentAccount);
        logOut();
        System.out.println(CLOSE_ACCOUNT);
    }


    private void showBalance() {
        System.out.printf(BALANCE, currentAccount.getBalance());
        System.out.println();
    }

    private void bye() {
        System.out.println(BYE);
        bs.close();
        System.exit(0);
    }

    private void logOut() {
        currentAccount = null;
        System.out.println(SUC_LOG_OUT);
    }
}