package client.commands;

import client.Client;
import client.exceptions.NetworkClientException;
import client.network.NetworkClient;
import models.Movie;
import requests.FilterContainsNameRequest;
import responses.Response;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class FilterContainsName extends AbstractCommand{
    public static final int ARGS_LENGTH = 1;
    public static final String DESCRIPTION = "������� ��������, �������� ���� name ������� �������� �������� ���������";
//    private final ArrayDeque<Movie> movies;
    private final String name;
    public FilterContainsName(Client client, NetworkClient networkClient, String name) {
        super("filterContainsName", client, networkClient);
        this.name=name;
    }

    @Override
    public Response execute() throws NetworkClientException {
        FilterContainsNameRequest request = new FilterContainsNameRequest(name);
        Response response = networkClient.sendRequest(request);
        return response;
    }
}
