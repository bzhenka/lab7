package responses;

import models.Movie;

import java.io.Serial;

public class HeadResponse extends Response{
    @Serial
    private static final long serialVersionUID = 1L;
    public final Movie movie;
    public HeadResponse(String error, Movie movie) {
        super("Head", error);
        this.movie = movie;
    }
}
