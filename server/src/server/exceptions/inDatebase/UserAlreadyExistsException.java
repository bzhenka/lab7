package server.exceptions.inDatebase;

public class UserAlreadyExistsException extends AllDataBaseException{
    public UserAlreadyExistsException() {
        super("пользователь уже существует");
    }
}
