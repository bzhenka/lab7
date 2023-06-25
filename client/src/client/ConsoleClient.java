package client;

import client.form.*;
import client.network.NetworkClient;
import client.network.TCPClient;
import enums.*;
import client.commands.*;
import client.exceptions.*;
import client.reader.*;
import responses.*;
import server.exceptions.*;
import models.*;

import java.io.IOException;
import java.util.*;

public class ConsoleClient implements Client {
    private static final String HOST = "localhost";
    private static final int PORT = 3683;
    private Reader reader = new ConsoleReader();
    private boolean stopWorker = false;
    private Set<String> paths = new HashSet<>();
    private final NetworkClient networkClient;

    public ConsoleClient() {
        networkClient = new TCPClient(HOST, PORT);
        System.out.println("Можете вводить команды");
    }

    public void worker() {
        while (!stopWorker) {
            try {
                String line = reader.readLine().trim();
                executeCommand(line);

            } catch (WrongCommandException | WrongArgumentsLengthException | IOException | WrongArgumentException |
                     InputInterruptionException | MovieNotFoundException | NetworkClientException e) {
                System.out.println("Неверные данные " + e.getMessage());
            }
        }
    }
    private void executeCommand(String line) throws WrongCommandException, WrongArgumentsLengthException, IOException,
            WrongArgumentException, InputInterruptionException, MovieNotFoundException, NetworkClientException {
        String[] splitLine = line.split(" ");
        String commandName = splitLine[0].toLowerCase();
        String[] arguments = Arrays.copyOfRange(splitLine, 1, splitLine.length);
        switch (commandName) {
                case "exit" -> {
                    if (arguments.length != Exit.ARGS_LENGTH) {
                        throw new WrongArgumentsLengthException();
                    }
                    Command command = new Exit(this, networkClient);
                    command.execute();
                }
                case "clear" -> {
                    if (arguments.length != Clear.ARGS_LENGTH) {
                        throw new WrongArgumentsLengthException();
                    }
                    Command command = new Clear(this, networkClient);
                    command.execute();
                    System.out.println("Коллекция очищена");
                }
                case "add" -> {
                    if (arguments.length != Add.ARGS_LENGTH) {
                        throw new WrongArgumentsLengthException();
                    }

                    String[] movieArgs = new MovieForm(reader).getData();
                    Command command = new Add(this, networkClient, movieArgs);
                    AddResponse response = (AddResponse) command.execute();
                    System.out.println("Фильм добавлен");
                }
//                case "save" -> {
//                    if (arguments.length != Save.ARGS_LENGTH){
//                        throw new WrongArgumentsLengthException();
//                    }
//                    Command command = new Save(this, networkClient);
//                    command.execute();
//                    System.out.println("Фильм сохранен");
//                }
                case "show" -> {
                    if (arguments.length != Show.ARGS_LENGTH){
                        throw new WrongArgumentsLengthException();
                    }
                    Command command = new Show(this, networkClient);
                    ShowResponse response = (ShowResponse) command.execute();
                    ArrayDeque<Movie> movies = response.movieDeque;
                    if (movies.size() == 0){
                        System.out.println("В коллекции нет фильмов");
                    }
                    FilmConsole.printMovie(movies);
                }
                case "updateid" -> {
                    if (arguments.length != UpdateId.ARGS_LENGTH){
                        throw new WrongArgumentsLengthException();
                    }
                    Integer id;
                    try {
                        id = Integer.parseInt(arguments[0]);
                    } catch (NumberFormatException e) {
                        throw new WrongCommandException(", id указан не верно");
                    }

                    String[] movieArgs = new MovieForm(reader).getData();
                    Command command = new UpdateId(this, networkClient, id, movieArgs);
                    command.execute();
                    System.out.println("Фильм с id " + id + " обновлен");
                }
                case "head" -> {
                    if (arguments.length != Head.ARGS_LENGTH){
                        throw new WrongArgumentsLengthException();
                    }
                    Head command = new Head(this, networkClient);
                    HeadResponse response = (HeadResponse) command.execute();
                    FilmConsole.oneMovie(response.movie);
                }
                case "removebyid" -> {
                    if (arguments.length != RemoveById.ARGS_LENGTH){
                        throw new WrongArgumentsLengthException();
                    }
                    int id = Integer.parseInt(arguments[0]);
                    Command command = new RemoveById(this, networkClient, id);
                    RemoveByIdResponse response = (RemoveByIdResponse) command.execute();
                    boolean result = response.removed;
                    if (result) {
                        System.out.println("Фильм с id " + id + " удален");
                    } else {
                        System.out.println("Фильм с id " + id + " не удален");
                    }
                }
                case "removefirst" -> {
                    if (arguments.length != RemoveFirst.ARGS_LENGTH){
                        throw new WrongArgumentsLengthException();
                    }
                    Command command = new RemoveFirst(this, networkClient);
                    RemoveFirstResponse response = (RemoveFirstResponse) command.execute();
                    if (response.error == null) {
                        System.out.println(ConsoleColor.BLUE.getCode() + "Первый фильм удален");
                    } else {
                        System.out.println(response.error);
                    }
                }
                case "removehead" -> {
                    if (arguments.length != RemoveHead.ARGS_LENGTH){
                        throw new WrongArgumentsLengthException();
                    }
                    Command command = new RemoveHead(this, networkClient);
                    RemoveHeadResponse response = (RemoveHeadResponse) command.execute();
                    if (response.error == null) {
                        FilmConsole.oneMovie(response.movies);
                        System.out.println(ConsoleColor.BLUE.getCode() + "Фильм удален");
                    } else {
                        System.out.println(response.error);
                    }
                }
                case "countlessthanoperator" -> {
                    if (arguments.length != CountLessThanOperator.ARGS_LENGTH){
                        throw new WrongArgumentsLengthException();
                    }
                    String[] operatorArgs = new OperatorForm(reader).getData();
                    Command command = new CountLessThanOperator(this, networkClient, operatorArgs);
                    CountLessThanOperatorResponse response = (CountLessThanOperatorResponse) command.execute();
                    System.out.println(ConsoleColor.BLUE.getCode() + "Количество фильмов, значение поля operator которых меньше заданного: " + response.count);
                }
                case "filtercontainsname" -> {
                    if (arguments.length != FilterContainsName.ARGS_LENGTH){
                        throw new WrongArgumentsLengthException();
                    }
                    String name = arguments[0];
                    FilterContainsName command = new FilterContainsName(this, networkClient, name);
                    FilterContainsNameResponse response = (FilterContainsNameResponse) command.execute();
                    ArrayList<Movie> movies = response.movies;
                    if (movies.size() == 0){
                        System.out.println(ConsoleColor.PURPLE.getCode() + "Нет фильмов, которые содержат заданную подстроку");
                    }
                    FilmConsole.printMovie(movies);
                }
                case "filterlessthanoscarscount" -> {
                    if (arguments.length != FilterLessThanOscarsCount.ARGS_LENGTH){
                        throw new WrongArgumentsLengthException();
                    }
                    Long oscars;
                    try {
                        oscars = Long.valueOf(arguments[0]);
                    } catch (NumberFormatException e) {
                        throw new WrongCommandException(", количество оскаров указано не верно");
                    }
                    Command command = new FilterLessThanOscarsCount(this, networkClient, oscars);
                    FilterLessThanOscarsCountResponse response = (FilterLessThanOscarsCountResponse) command.execute();
                    ArrayDeque<Movie> movies = response.movies;
                    if (movies.size() == 0){
                        System.out.println(ConsoleColor.PURPLE.getCode() + "Нет фильмов меньше заданного количества оскаров");
                    }
                    FilmConsole.printMovie(movies);
                }
                case "info" -> {
                    if (arguments.length != Info.ARGS_LENGTH){
                        throw new WrongArgumentsLengthException();
                    }
                    Command command = new Info(this, networkClient);
                    InfoResponse response = (InfoResponse) command.execute();
                    String[] result = response.info;
                    System.out.println(ConsoleColor.BLUE.getCode() + "Тип коллекции: " + result[0] + ", Размер коллекции: " + result[1] + ", Дата инициализации: " + result[2]);
                }
                case "help" -> {
                    if (arguments.length != Help.ARGS_LENGTH){
                        throw new WrongArgumentsLengthException();
                    }
                    Command command = new Help(this, networkClient);
                    command.execute();
                }
                case "executescript" -> {
                    if (arguments.length != ExecuteScript.ARGS_LENGTH){
                        throw new WrongArgumentsLengthException();
                    }
                    String filepath = arguments[0];
                    Command command = new ExecuteScript(this, networkClient, filepath);
                    command.execute();
                }
                default -> {
                    throw new WrongCommandException(ConsoleColor.PURPLE.getCode() + "Неверная команда " + commandName);
                }

            }
    }

    @Override
    public Response exit() {
        stopWorker = true;
        return new EmptyResponse();
    }

    @Override
    public Response help() {
        System.out.println("Help: " + Help.DESCRIPTION);
        System.out.println("Info: " + Info.DESCRIPTION);
        System.out.println("Show: " + Show.DESCRIPTION);
        System.out.println("Add: " + Add.DESCRIPTION);
        System.out.println("UpdateId: " + UpdateId.DESCRIPTION);
        System.out.println("RemoveById: " + RemoveById.DESCRIPTION);
        System.out.println("Clear: " + Clear.DESCRIPTION);
        System.out.println("ExecuteScript: " + ExecuteScript.DESCRIPTION);
        System.out.println("Exit: " + Exit.DESCRIPTION);
        System.out.println("RemoveFirst: " + RemoveFirst.DESCRIPTION);
        System.out.println("Head: " + Head.DESCRIPTION);
        System.out.println("RemoveHead: " + RemoveHead.DESCRIPTION);
        System.out.println("CountLessThanOperator: " + CountLessThanOperator.DESCRIPTION);
        System.out.println("FilterContainsName: " + FilterContainsName.DESCRIPTION);
        System.out.println("FilterLessThanOscarsCount: " + FilterLessThanOscarsCount.DESCRIPTION);
        return new EmptyResponse();
    }

    @Override
    public Response executeScript(String filepath) {
        try {
            if (paths.contains(filepath)) {
                System.out.println("Рекурсия");

            }
            paths.add(filepath);

            reader = new ScriptReader(filepath);
            String line;
            while (reader.hasNextLine()) {
                line = reader.readLine();
                if (Objects.equals(line, "executeScript")){
                    System.out.println("Файл содержит команду executeScript, что приводит к рекурсии, удалите команду");
                    break;
                }
                executeCommand(line);
            }

            reader = new ConsoleReader();
            paths.remove(filepath);

        } catch (IOException | WrongCommandException | WrongArgumentsLengthException | WrongArgumentException |
                 InputInterruptionException | MovieNotFoundException | NetworkClientException e){
            System.err.println(ConsoleColor.PURPLE.getCode() + "Ошибка чтения файла: " + e.getMessage());
        }
        return new EmptyResponse();
    }
}
