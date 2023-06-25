package server.commands;

import requests.CountLessThanOperatorRequest;
import requests.Request;
import responses.CountLessThanOperatorResponse;
import responses.Response;
import server.Receiver;

import java.io.Serial;

public class CountLessThanOperator extends AbstractCommandResult<Integer>{
    @Serial
    private static final long serialVersionUID = 1L;
    public Integer result;
    public CountLessThanOperator(Receiver receiver) {
        super("countLessThanOperator", receiver);

    }

    @Override
    public Integer getResult() {
        return result;
    }

    @Override
    public Response execute(Request request) {
        CountLessThanOperatorRequest countLessThanOperatorRequest = (CountLessThanOperatorRequest) request;
        CountLessThanOperatorResponse response;
        result = receiver.countLessThanOperator(countLessThanOperatorRequest.operatorArgs);
        return new CountLessThanOperatorResponse(null, result);
    }
}
