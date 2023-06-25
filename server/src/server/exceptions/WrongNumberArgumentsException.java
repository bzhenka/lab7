package server.exceptions;

public class WrongNumberArgumentsException extends Exception {
    public WrongNumberArgumentsException() {
        super("неправильное количество аргументов");
    }
}
