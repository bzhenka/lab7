package dateBase;

import java.io.IOException;
import java.sql.SQLException;

public class PostgresDatabase extends DataBasePassword{
    private static final String URL = "jdbc:postgresql://localhost:5432/studs";

    public PostgresDatabase(String filepath) throws IOException {
        super(filepath);
    }

    @Override
    public ConnectionDateBase createConnection() throws SQLException {
        return new ConnectionDateBase(URL, login, password);
    }
}
