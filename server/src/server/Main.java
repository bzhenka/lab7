package server;

import server.exceptions.EnvironmentVariableException;
import server.exceptions.FilePermissionException;
import server.exceptions.IncorrectFileException;
import server.exceptions.InvalidFileDataException;
import server.networkServer.TCPServer;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        try {
            Receiver receiver = new Receiver();
            TCPServer server = new TCPServer(receiver);


            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    server.handleExit();
                    System.out.println("Shutting down ...");
                }
            });
//            ne poluchilos

            server.run();
        } catch (FilePermissionException | IncorrectFileException | IOException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }
}
//thank you