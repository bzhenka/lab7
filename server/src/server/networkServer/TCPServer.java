package server.networkServer;

import jdk.net.ExtendedSocketOptions;
import requests.ExitRequest;
import requests.Request;
import requests.SaveRequest;
import responses.EmptyResponse;
import responses.Response;
import responses.ServerErrorResponse;
import server.ClientCommandHandler;
import server.Receiver;
import server.ServerCommandHandler;
import server.io.BufferedConsoleReader;
import server.exceptions.InvalidCommandException;
import server.exceptions.InvalidRequestException;
import server.exceptions.WrongNumberArgumentsException;
import server.io.ConsoleReader;
import server.io.Reader;

import java.io.*;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TCPServer implements NetworkServer {

    private static final int BUFFER_SIZE = 4096;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    private static final String HOST = "localhost";
    private static final int PORT = 3683;

    private ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final ClientCommandHandler clientCommandHandler;
    private final ServerCommandHandler serverCommandHandler;
    private final ForkJoinPool sendPool = new ForkJoinPool();
    private final Receiver receiver;

    private boolean canExit = false;
    private final Reader reader = new ConsoleReader();

    public TCPServer(Receiver receiver) throws IOException {
        this.clientCommandHandler = new ClientCommandHandler(receiver);
        this.serverCommandHandler = new ServerCommandHandler(receiver, this);

        this.openConnection();
        this.selector = initSelector();
        this.receiver = receiver;

        System.out.println(
                "Server app launched.\n" +
                        "You can use save and exit commands in interactive mode.");
    }



    private void openConnection() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.setOption(ExtendedSocketOptions.TCP_KEEPIDLE, 10);
        InetSocketAddress address = new InetSocketAddress(HOST, PORT);
        try {
            serverSocketChannel.bind(address);
        } catch (BindException e) {
            String message = "Unable to use address " + address.getHostName() + ":" + address.getPort() + " - " + e.getMessage();
            throw new BindException(message);
        }
    }

    private Selector initSelector() throws IOException {
        Selector socketSelector = SelectorProvider.provider().openSelector();
        serverSocketChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
        return socketSelector;
    }

    public void run() throws IOException {
        try {
            while (!canExit) {
                selector.selectNow();
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();

                    if (key.isValid()) {
                        if (key.isAcceptable()) {
                            accept(key);
                        } else if (key.isReadable()) {
                            read(key);
                        } else if (key.isWritable()) {
                            write(key);
                        }
                    }
                }

                try {
                    BufferedConsoleReader reader = new BufferedConsoleReader();

                    if (reader.ready()) {
                        String input = reader.readLine().trim();
                        if (input.startsWith("//") || input.equals("")) {
                            continue;
                        }
                        String[] inputArray = input.split(" +");
                        String commandName = inputArray[0].toLowerCase();

                        String[] args = new String[inputArray.length - 1];
                        System.arraycopy(inputArray, 1, args, 0, inputArray.length - 1);

                        switch (commandName) {
                            case "save" -> {
                                if (args.length != 0)
                                    throw new WrongNumberArgumentsException();

                                Request request = new SaveRequest();
                                serverCommandHandler.handle(request);
                                System.out.println("*collection saved successfully*");
                            }
                            case "exit" -> {
                                if (args.length != 0)
                                    throw new WrongNumberArgumentsException();

                                Request request = new ExitRequest();
                                serverCommandHandler.handle(request);
                            }
                            default -> throw new InvalidCommandException(commandName);
                        }
                    }
                } catch (WrongNumberArgumentsException | InvalidCommandException e) {
                    System.out.println(e.getMessage());
                }
            }
        } finally {
            handleExit();
        }
    }

    public void handleExit() {
        silentCloseConnection();
        Request request = new SaveRequest();
        serverCommandHandler.handle(request);
    }

    private void accept(SelectionKey key) throws IOException {
        var ssc = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = ssc.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

    }

    private void read(SelectionKey key) throws IOException {
        Runnable readTask = new Runnable() {
            @Override
            public void run() {
                try {
                    SocketChannel sc = (SocketChannel) key.channel();
                    readBuffer.clear();
                    int bytesRead;
                    try {
                        bytesRead = sc.read(readBuffer);
                    } catch (IOException e) {
                        key.cancel();
                        sc.close();
                        return;
                    }
                    if (bytesRead == -1) {
                        key.cancel();
                        return;
                    }
                    String input = new String(readBuffer.array(), 0, bytesRead, StandardCharsets.UTF_8);
                    createResponse(sc);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        Thread readThread = new Thread(readTask);
        readThread.start();

    }
    private void createResponse(SocketChannel sc) {
        Runnable createResponseTask = new Runnable() {
            @Override
            public void run() {
                try {
                    Response response;
                    try {
                        response = handleRequest(readBuffer);
                    } catch (ClassNotFoundException e) {
                        response = new ServerErrorResponse(e.getMessage());
                    }
                    sc.register(selector, SelectionKey.OP_WRITE, response);
                } catch (IOException e) {
                    throw new RuntimeException(e);

                }
            }

        };
        Thread createResponseThread = new Thread(createResponseTask);
        createResponseThread.start();
    }



    private Response handleRequest(ByteBuffer buffer) throws IOException, ClassNotFoundException {
        Request request;
        Response response;
        try {
            request = (Request) deserializeObject(buffer);
            if (request == null) {
                throw new InvalidRequestException("Request is null");
            }
            response = clientCommandHandler.handle(request);
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Unknown request type");
        } catch (InvalidRequestException e) {
            response = new ServerErrorResponse(e.getMessage());
        }

        return response;
    }

    private void write(SelectionKey key) {
        RecursiveAction writeAction = new RecursiveAction() {
            @Override
            protected void compute() {
                try {
                    SocketChannel sc = (SocketChannel) key.channel();
                    Response response = (Response) key.attachment();

                    ByteBuffer writeBuffer = serializeObject(response);
                    writeBuffer.flip();
                    while (writeBuffer.hasRemaining()) {
                        sc.write(writeBuffer);
                    }

                    sc.register(selector, SelectionKey.OP_READ);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
    };
    sendPool.execute(writeAction);
    }

    private ByteBuffer serializeObject(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);

        ByteBuffer buffer = ByteBuffer.allocate(baos.size());
        buffer.put(baos.toByteArray());
        return buffer;
    }

    private Object deserializeObject(ByteBuffer buffer) throws IOException, ClassNotFoundException, InvalidRequestException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array());
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }  catch (StreamCorruptedException e) {
            String message = "Unknown request";
            throw new InvalidRequestException(message);
        } catch (EOFException e) {
            String message = "Request is too big";
            throw new InvalidRequestException(message);
        }
    }

    private void closeConnection() throws IOException {
        if (serverSocketChannel != null) {
            serverSocketChannel.close();
        }
    }

    private void silentCloseConnection() {
        try {
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Response exit() {
        canExit = true;
        return new EmptyResponse();
    }
}


