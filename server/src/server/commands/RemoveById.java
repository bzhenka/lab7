package server.commands;
import requests.RemoveByIdRequest;
import requests.Request;
import responses.RemoveByIdResponse;
import responses.Response;
import server.Receiver;

import java.io.Serial;

public class RemoveById extends AbstractCommandResult<Boolean>{
    @Serial
    private static final long serialVersionUID = 1L;

    public RemoveById(Receiver receiver) {
        super("removeById", receiver);

    }

    @Override
    public Response execute(Request request) {
        RemoveByIdRequest remidRequest = (RemoveByIdRequest) request;
        RemoveByIdResponse response;
        Boolean removed = receiver.removeById(remidRequest.id);
        response = new RemoveByIdResponse(null, removed);
        return response;
    }

    @Override
    public Boolean getResult() {
        return result;
    }


}
