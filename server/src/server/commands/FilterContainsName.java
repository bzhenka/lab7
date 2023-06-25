package server.commands;
import models.Movie;
import requests.FilterContainsNameRequest;
import requests.Request;
import responses.FilterContainsNameResponse;
import responses.FilterLessThanOscarsCountResponse;
import responses.Response;
import server.Receiver;

import java.io.Serial;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class FilterContainsName extends AbstractCommandResult<ArrayList<Movie>> {
    @Serial
    private static final long serialVersionUID = 1L;
    private ArrayList<Movie> result = null;

    public FilterContainsName(Receiver receiver) {
        super("filterContainsName", receiver);
    }


    @Override
    public ArrayList<Movie> getResult() {
        return result;
    }

    @Override
    public Response execute(Request request) {
        FilterContainsNameRequest filterContainsName = (FilterContainsNameRequest) request;
        result = receiver.filterContainsName(filterContainsName.name);
        FilterContainsNameResponse response = new FilterContainsNameResponse(null, result);
        return response;
    }
}
