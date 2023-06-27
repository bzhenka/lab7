package server.exceptions.inDatebase;

public class UserNotFoundException extends AllDataBaseException{
    public UserNotFoundException() {
        super("Пользователь не найден");
    }
}
