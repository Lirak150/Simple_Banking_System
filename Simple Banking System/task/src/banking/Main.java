package banking;

import java.sql.SQLException;


public class Main {
    public static void main(String[] args) {
        try (SQLHelper helper = new SQLHelper(args[1]);
             BankingSystem system = new BankingSystem(helper)) {
            OperationsWithBS op = new OperationsWithBS(system);
            op.start();
        } catch (SQLException e) {
            System.out.println("Problems with database");
            e.printStackTrace();
        }
    }
}

