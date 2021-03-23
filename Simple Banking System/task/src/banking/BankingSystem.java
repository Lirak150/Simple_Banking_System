package banking;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

class BankingSystem implements AutoCloseable {
    private static final String BIN = "400000";
    private long pointer = 1;
    private final Random random = new Random();
    private static final int PIN_LENGTH = 4;
    private static final int CARD_LENGTH = 16;
    private final SQLHelper sqlHelper;
    private static final String NAME_FILE = "pointer";

    private static final String NOT_EN_MONEY = "Not enough money!\n";
    private static final String TRANSFER_TO_SAME_ACCOUNT = "You can't transfer money to the same account!\n";
    private static final String MISTAKE_CARD = "Probably you made a mistake in the card number. Please try again!\n";
    private static final String CARD_NOT_EXIST = "Such a card does not exist.";

    public BankingSystem(SQLHelper sqlHelper) {
        this.sqlHelper = sqlHelper;
        init();
    }

    public Account addAccount() {
        long id = pointer++;
        int pin = random.nextInt(9999) + 1;
        Account account = new Account(id, 0, generateStringPin(pin));
        account.setControlDigit(generateControlDigit(id));
        account.setCard(generateCard(id, account.getControlDigit()));
        try {
            sqlHelper.addAccount(account);
            return account;
        } catch (SQLException e) {
            System.out.println("Problems with database ");
            e.printStackTrace();
            System.exit(1);
        }
        throw new UnsupportedOperationException();
    }

    private String generateCard(long id, int controlDigit) {
        return BIN + "0".repeat(CARD_LENGTH - BIN.length() -
                Long.toString(id).length() - 1) + id + controlDigit;
    }

    private long generateId(String card) {
        StringBuilder builder = new StringBuilder(card);
        builder.delete(0, BIN.length());
        builder.deleteCharAt(builder.length() - 1);
        int j = 0;
        while (j < builder.length() && builder.charAt(j) == '0') {
            builder.deleteCharAt(j);
        }
        return Long.parseLong(builder.toString());
    }

    private int generateControlDigit(long id) {
        String dropLastDigit = BIN + "0".repeat(CARD_LENGTH - BIN.length() - Long.toString(id).length() - 1) + id;
        char[] charArray = dropLastDigit.toCharArray();
        int sum = 0;
        for (int i = 0; i < charArray.length; i++) {
            int k = Character.digit(charArray[i], 10);
            if (i % 2 == 0) {
                k *= 2;
                if (k > 9) {
                    k -= 9;
                }
            }
            sum += k;
        }
        return (10 - sum % 10) % 10;
    }

    private boolean checkControlDigit(String card) {
        int controlDigit = Character.digit(card.charAt(CARD_LENGTH - 1), 10);
        String dropLastDigit = card.substring(0, CARD_LENGTH - 1);
        char[] charArray = dropLastDigit.toCharArray();
        int sum = 0;
        for (int i = 0; i < charArray.length; i++) {
            int k = Character.digit(charArray[i], 10);
            if (i % 2 == 0) {
                k *= 2;
                if (k > 9) {
                    k -= 9;
                }
            }
            sum += k;
        }
        int controlLuna = (10 - sum % 10) % 10;
        return controlDigit == controlLuna;
    }

    private String generateStringPin(int pin) {
        return "0".repeat(PIN_LENGTH - Integer.toString(pin).length())
                + pin;
    }

    public Optional<Account> getAccount(long accountId, String pinStr) {
        try {
            Optional<Account> opAccount = sqlHelper.getAccount(accountId);
            if (opAccount.isEmpty() || !opAccount.get().getPin().equals(pinStr)) {
                return Optional.empty();
            } else {
                return opAccount;
            }
        } catch (SQLException e) {
            System.out.println("Problems with database ");
            e.printStackTrace();
            System.exit(1);
        }
        throw new UnsupportedOperationException();
    }

    public Optional<Account> getAccount(String card, String pinStr) {
        StringBuilder cardBuilder = new StringBuilder(card);
        if (card.length() != CARD_LENGTH || pinStr.length() != PIN_LENGTH) {
            return Optional.empty();
        }
        cardBuilder.delete(0, BIN.length());
        cardBuilder.deleteCharAt(cardBuilder.length() - 1);
        int j = 0;
        while (j < cardBuilder.length() && cardBuilder.charAt(j) == '0') {
            cardBuilder.deleteCharAt(j++);
        }
        return getAccount(Long.parseLong(cardBuilder.toString()), pinStr);
    }

    @Override
    public void close() {
        System.out.println("close");
        try (FileOutputStream stream = new FileOutputStream("pointer")) {
            byte[] l = ByteUtils.longToBytes(pointer);
            stream.write(l);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        try {
            File file = new File(NAME_FILE);
            if (!file.createNewFile()) {
                FileInputStream stream = new FileInputStream(file);
                byte[] l = new byte[8];
                stream.read(l);
                pointer = ByteUtils.bytesToLong(l);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeAccount(Account currentAccount) {
        try {
            sqlHelper.closeAccount(currentAccount.getId());
        } catch (SQLException e) {
            System.out.println("Problems with database");
            e.printStackTrace();
        }
    }

    public void addIncome(int deposit, Account currentAccount) {
        try {
            sqlHelper.addIncome(deposit, currentAccount.getId());
        } catch (SQLException e) {
            System.out.println("Problems with database");
            e.printStackTrace();
        }
    }

    public List<String> tryOperation(Account currentAccount, String cardToTransfer) {
        List<String> errors = new ArrayList<>();
        if (currentAccount.getCard().equals(cardToTransfer)) {
            errors.add(TRANSFER_TO_SAME_ACCOUNT);
            return errors;
        }
        if (!checkControlDigit(cardToTransfer)) {
            errors.add(MISTAKE_CARD);
        }
        try {
            if (!sqlHelper.cardExist(generateId(cardToTransfer))) {
                errors.add(CARD_NOT_EXIST);
            }
        } catch (SQLException e) {
            System.out.println("Problems with database");
            e.printStackTrace();
        }

        return errors;
    }

    public Optional<String> checkDeposit(Account account, int deposit) {
        boolean haveEnoughMoney = account.getBalance() >= deposit;
        if (!haveEnoughMoney) {
            return Optional.of(NOT_EN_MONEY);
        }
        return Optional.empty();
    }

    public boolean makeOperation(int deposit, Account fromAccount, String toAccountCard) {
        try {
            sqlHelper.reduceSum(fromAccount.getId(), deposit);
            sqlHelper.addIncome(deposit, generateId(toAccountCard));
            fromAccount.setBalance(fromAccount.getBalance() - deposit);
            return true;
        } catch (SQLException e) {
            System.out.println("Problems with database");
            e.printStackTrace();
            return false;
        }
    }
}