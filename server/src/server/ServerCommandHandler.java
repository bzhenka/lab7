package server;

import requests.Request;
import responses.Response;
import server.commands.Command;
import server.commands.Exit;
import server.commands.Save;
import server.networkServer.NetworkServer;

import java.util.HashMap;

public class ServerCommandHandler {
    final private HashMap<String, Command> commands = new HashMap<>();

    public ServerCommandHandler(Receiver receiver, NetworkServer server) {
        registerCommand("save", new Save(receiver));
        registerCommand("exit", new Exit(receiver, server));
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
