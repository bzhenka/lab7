package client.commands;


import client.Client;
import client.exceptions.NetworkClientException;
import client.network.NetworkClient;
import requests.AddRequest;
import requests.Request;
import responses.Response;

public class Add extends AbstractCommand{
    public final static int ARGS_LENGTH = 0;
    public static final String DESCRIPTION ="добавить новый элемент в коллекцию";
    private final String[] movieArguments;


    public Add(Client client, NetworkClient networkClient, String[] movieArguments) {
        super("add", client, networkClient);
        this.movieArguments = movieArguments;
    }


    @Override
    public Response execute() throws NetworkClientException {
        Request request = new AddRequest(movieArguments);
        Response response = networkClient.sendRequest(request);
        return response;
    }
}



