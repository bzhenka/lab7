package server.commands;
import requests.Request;
import responses.Response;
import server.Receiver;

import java.io.IOException;

public class Save extends AbstractCommand{
    public Save(Receiver receiver) {
        super("save", receiver);
    }


    @Override
    public Response execute(Request request) {
        try {
            receiver.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

