package banking;

class Account {
    private final long id;
    private int balance;
    private String card;
    private String pin;
    private int controlDigit;


    public Account(long id, int balance, String pin, String cardNumber) {
        this.id = id;
        this.balance = balance;
        this.pin = pin;
        this.card = cardNumber;
    }

    public Account(long id, int balance, String pin) {
        this(id, balance, pin, null);
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public int getControlDigit() {
        return controlDigit;
    }

    public void setControlDigit(int controlDigit) {
        this.controlDigit = controlDigit;
    }

    public long getId() {
        return id;
    }
}