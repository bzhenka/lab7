package client.exceptions;

public class MovieNotFoundException extends Exception{
    public MovieNotFoundException(int id) {
        super("����� � ������ id " + id + " �� ������");
    }
}
