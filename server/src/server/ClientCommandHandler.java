package server;

import requests.Request;
import responses.Response;
import server.commands.*;

import java.util.HashMap;

public class ClientCommandHandler {

    final private HashMap<String, Command> commands = new HashMap<>();
    private final Receiver receiver;

    public ClientCommandHandler(Receiver receiver) {
        this.receiver = receiver;
        registerCommand("info", new Info(receiver));
        registerCommand("show", new Show(receiver));
        registerCommand("add", new Add(receiver));
        registerCommand("UpdateId", new UpdateId(receiver));
        registerCommand("removeById", new RemoveById(receiver));
        registerCommand("clear", new Clear(receiver));
        registerCommand("RemoveFirst", new RemoveFirst(receiver));
        registerCommand("Head", new Head(receiver));
        registerCommand("RemoveHead", new RemoveHead(receiver));
        registerCommand("CountLessThanOperator", new CountLessThanOperator(receiver));
        registerCommand("FilterContainsName", new FilterContainsName(receiver));
        registerCommand("FilterLessThanOscarsCount", new FilterLessThanOscarsCount(receiver));

    }

    private void registerCommand(String name, Command command) {
        commands.put(name, command);
    }

    public Response handle(Request request) {
        Command command = commands.get(request.name);
        Response response = command.execute(request);
        return response;
    }
}
