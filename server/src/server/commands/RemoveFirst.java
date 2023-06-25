package server.commands;

import requests.RemoveFirstRequest;
import requests.Request;
import responses.RemoveFirstResponse;

import responses.Response;
import server.Receiver;
import server.exceptions.WrongArgumentException;

import java.io.Serial;
import java.util.NoSuchElementException;

public class RemoveFirst extends AbstractCommand{
    @Serial
    private static final long serialVersionUID = 1L;
    public RemoveFirst(Receiver receiver) {
        super("removeFirst", receiver);
    }

    @Override
    public Response execute(Request request) {
        RemoveFirstRequest removefirstRequest = (RemoveFirstRequest) request;
        RemoveFirstResponse response;
        try {
            receiver.removeFirst();
            response = new RemoveFirstResponse(null);
        } catch (WrongArgumentException e) {
            response = new RemoveFirstResponse(e.getMessage());
        }
        return response;
    }
}
