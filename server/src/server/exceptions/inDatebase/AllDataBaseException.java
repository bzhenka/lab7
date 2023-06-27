package server.exceptions.inDatebase;

import java.sql.SQLException;

public class AllDataBaseException extends SQLException {
    public AllDataBaseException (String reason) {
        super(reason);
    }
}

