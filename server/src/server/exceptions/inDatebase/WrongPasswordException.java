package server.exceptions.inDatebase;

import dateBase.DataBasePassword;

public class WrongPasswordException extends AllDataBaseException {
    public WrongPasswordException() {
        super("Неправильный пароль");
    }
}
