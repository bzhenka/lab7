package dateBase;

import enums.Color;
import enums.Country;
import enums.MovieGenre;
import enums.MpaaRating;
import models.Movie;
import server.exceptions.InvalidDataException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;


public abstract class AbstractConnectionDateBase { //класс для работы с базой данный, методы которого предназначены для выполнения операций с базой данных, таких как аутентификация пользователя, добавление объектов, обновление данных, проверка владения определенными ключами или идентификаторами и т.д.
    protected Connection connection;
    public AbstractConnectionDateBase(String url, String login, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, login, password);
    }
    public abstract void authenticateUser(String login, String password) throws SQLException; //авторизовать
    public abstract void addUser(String login, String password) throws SQLException; //добавить пользователя
    public abstract ArrayDeque<Movie> getAllMovies() throws SQLException, InvalidDataException;
    public abstract int addObject(String login, String movieName, Double x, Float y, long oscarsCount, MovieGenre movieGenre,
                                  MpaaRating mpaaRating, String operatorName, Integer weight, Integer age, Color eyecolor, Color haircolor, Country nationality, Double X, Float Y, Integer Z) throws SQLException;
    public abstract void updateObjectById(Integer movieId, String movieName, Double x, Float y, long oscarsCount, MovieGenre movieGenre,
                                  MpaaRating mpaaRating, String operatorName, Integer weight, Integer age, Color eyecolor, Color haircolor, Country nationality, Double X, Float Y, Integer Z) throws SQLException;
    public abstract boolean isMovieIDOwnedByUser(Integer id, String login);
    public abstract ArrayList<Integer> clearCollectionForUser(String login) throws SQLException;

}
