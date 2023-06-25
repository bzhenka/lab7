package server.commands;
import models.Movie;
import requests.Request;
import responses.RemoveFirstResponse;
import responses.RemoveHeadResponse;
import responses.Response;
import server.Receiver;
import server.exceptions.WrongArgumentException;

import java.io.Serial;
import java.util.NoSuchElementException;

public class RemoveHead extends AbstractCommandResult<Movie> {
    @Serial
    private static final long serialVersionUID = 1L;
    private Movie result = null;
    public RemoveHead(Receiver receiver) {
        super("removeHead", receiver);
    }


    @Override
    public Movie getResult() {
        return result;
    }

    @Override
    public Response execute(Request request) {
        RemoveHeadResponse removeHeadResponse;
        try {
            result = receiver.removeHead();
            removeHeadResponse = new RemoveHeadResponse(result, null);
        } catch (WrongArgumentException e) {
            removeHeadResponse = new RemoveHeadResponse(null, e.getMessage());
        }
        return removeHeadResponse;
    }
}
