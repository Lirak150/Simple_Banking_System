package banking;

import java.sql.*;
import java.util.Optional;

public class SQLHelper implements AutoCloseable {
    private final String url;
    private Connection connection;

    private PreparedStatement stClose;
    private PreparedStatement stAddIncome;
    private PreparedStatement stCardExist;
    private PreparedStatement stReduceSum;
    private PreparedStatement stAddAccount;
    private PreparedStatement stGetAccount;

    public SQLHelper(String fileName) throws SQLException {
        url = "jdbc:sqlite:" + fileName;
        init();
    }

    private void init() throws SQLException {
        connection = DriverManager.getConnection(url);
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(id INTEGER PRIMARY KEY, number TEXT," +
                " pin TEXT, balance INTEGER DEFAULT 0)");
    }

    public void addAccount(Account account) throws SQLException {
        if (stAddAccount == null) {
            stAddAccount = connection.prepareStatement("INSERT INTO card (id, number, pin, balance) " +
                    "VALUES (?, ?, ?, ?)");
        }
        stAddAccount.setLong(1, account.getId());
        stAddAccount.setString(2, account.getCard());
        stAddAccount.setString(3, account.getPin());
        stAddAccount.setInt(4, account.getBalance());
        stAddAccount.execute();
    }

    public Optional<Account> getAccount(long id) throws SQLException {
        if (stGetAccount == null) {
            stGetAccount = connection.prepareStatement("SELECT id, number, pin, balance FROM card WHERE id=?");
        }
        stGetAccount.setLong(1, id);
        ResultSet query = stGetAccount.executeQuery();
        if (query.next()) {
            String pin = query.getString("pin");
            int balance = query.getInt("balance");
            String numberCard = query.getString("number");
            Account account = new Account(id, balance, pin, numberCard);
            return Optional.of(account);
        } else {
            return Optional.empty();
        }
    }
    @Override
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        if (stClose != null) {
            stClose.close();
        }
        if (stAddIncome != null) {
            stAddAccount.close();
        }
        if (stCardExist != null) {
            stCardExist.close();
        }
        if (stReduceSum != null) {
            stReduceSum.close();
        }
        if (stAddAccount != null) {
            stAddAccount.close();
        }
        if (stGetAccount != null) {
            stGetAccount.close();
        }

    }

    public void closeAccount(long id) throws SQLException {
        if (stClose == null) {
            stClose = connection.prepareStatement("DELETE FROM card WHERE id = ?;");
        }
        stClose.setLong(1, id);
        stClose.execute();
    }

    public void addIncome(int deposit, long id) throws SQLException {
        if (stAddIncome == null) {
            stAddIncome = connection.prepareStatement("UPDATE card SET balance = balance + ? WHERE id = ?");
        }
        stAddIncome.setInt(1, deposit);
        stAddIncome.setLong(2, id);
        stAddIncome.execute();
    }

    public boolean cardExist(long id) throws SQLException {
        if (stCardExist == null) {
            stCardExist = connection.prepareStatement("SELECT * FROM card WHERE id = ?;");
        }
        stCardExist.setLong(1, id);
        ResultSet set = stCardExist.executeQuery();
        return set.next();
    }

    public void reduceSum(long id, int deposit) throws SQLException {
        if (stReduceSum == null) {
            stReduceSum = connection.prepareStatement("UPDATE card SET balance = balance - ? WHERE id = ?;");
        }
        stReduceSum.setInt(1, deposit);
        stReduceSum.setLong(2, id);
        stReduceSum.execute();
    }
}
