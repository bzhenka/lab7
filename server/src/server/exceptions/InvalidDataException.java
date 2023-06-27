package server.exceptions;

public class InvalidDataException extends IOException {
    public InvalidDataException(String path, String message) {
        super("! invalid data in " + path + ": " + message + " !");
    }
    public InvalidDataException(String message) {
        super("! invalid data: " + message + " !");
    }
}
