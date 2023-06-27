package dateBase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public abstract class DataBasePassword {
    private final static int LOGIN_INDEX = 3;
    private final static int PASSWORD_INDEX = 4;
    protected final String login;
    protected final String password;

    protected DataBasePassword(String filepath) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(new BufferedReader(new FileReader(filepath)))){
            String[] lines = scanner.nextLine().split(":");
            this.login = lines[LOGIN_INDEX];
            this.password = lines[PASSWORD_INDEX];
        }

    }
    abstract public ConnectionDateBase createConnection() throws SQLException, IOException;
}
