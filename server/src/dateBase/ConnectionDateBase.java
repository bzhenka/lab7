package dateBase;

import enums.Color;
import enums.Country;
import enums.MovieGenre;
import enums.MpaaRating;
import models.Coordinates;
import models.Location;
import models.Movie;
import models.Person;
import server.exceptions.InvalidDataException;
import server.exceptions.WrongArgumentException;
import server.exceptions.inDatebase.UserAlreadyExistsException;
import server.exceptions.inDatebase.UserNotFoundException;
import server.exceptions.inDatebase.WrongPasswordException;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Objects;

public class ConnectionDateBase extends AbstractConnectionDateBase{

    public ConnectionDateBase(String url, String login, String password) throws SQLException {
        super(url, login, password);
    }

    @Override
    public void authenticateUser(String login, String password) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM client WHERE login = ?");
        ps.setString(1, login);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            byte[] salt = result.getBytes("salt");
            byte[] expectedPassword = result.getBytes("password");

            if (!PasswordClient.isExpectedPassword(passwordBytes, salt, expectedPassword)) {
                throw new WrongPasswordException();
            }
        } else {
            throw new UserNotFoundException();
        }
    }



    @Override
    public void addUser(String login, String password) throws SQLException {
        if (findUser(login)) {
            throw new UserAlreadyExistsException();
        }

        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO client (login, password, salt) VALUES (?, ?, ?)");

        byte[] salt = PasswordClient.getSalt(); //делаем соль
        byte[] passwordHash = PasswordClient.hash(password.getBytes(StandardCharsets.UTF_8), salt); //хэшируем пароль вместе с солью

        ps.setString(1, login);
        ps.setBytes(2, passwordHash);
        ps.setBytes(3, salt);

        ps.executeUpdate();
    }
    private boolean findUser(String login) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM client WHERE login = ?");
        ps.setString(1, login);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            return true;
        }
        return false;
    }

    @Override
    public ArrayDeque<Movie> getAllMovies() throws SQLException, InvalidDataException {
        ArrayDeque<Movie> movie = new ArrayDeque<>();

            PreparedStatement ps = connection.prepareStatement(
                    "SELECT m.*, p.name AS OperatorName, p.weight, p.age, p.eyecolor, p.haircolor, p.nationality, p.x, p.y, p.z FROM movie m " +
                            "JOIN person p on m.operator = p.id"
            );

            ResultSet resultSet = ps.executeQuery();
            int i = 1;

            try {
                while (resultSet.next()) {
                    Movie movies = resultSetToMovie(resultSet);
                    movie.add(movies);
                    i += 1;
                }
            } catch (WrongArgumentException e) {
                StringBuilder errorMessage = new StringBuilder(e.getMessage());
                errorMessage.delete(0, 2);
                errorMessage.delete(errorMessage.length() - 2, errorMessage.length());
                throw new InvalidDataException("movie №" + i + ": " + errorMessage);
            }

            return movie;

    }

    @Override
    public int addObject(String login, String movieName, Double x, Float y, long oscarsCount, MovieGenre movieGenre, MpaaRating mpaaRating, String operatorName, Integer weight, Integer age, Color eyecolor, Color haircolor, Country nationality, Double X, Float Y, Integer Z) throws SQLException {
        int operator = addPerson(operatorName, weight, age, eyecolor, haircolor, nationality, X, Y, Z);
        int movieID = addMovie(movieName, x, y, oscarsCount, movieGenre, mpaaRating, operator);
        addOwner(movieID, login);
        return movieID;
    }

    @Override
    public void updateObjectById(Integer movieId, String movieName, Double x, Float y, long oscarsCount, MovieGenre genre, MpaaRating mpaarating, String operator, Integer weight, Integer age, Color eyecolor, Color haircolor, Country nationality, Double X, Float Y, Integer Z) throws SQLException {
        int operatorID = findOperatorIDByMovie(movieId);
        updatePerson(operatorID, operator, weight, age, eyecolor, haircolor, nationality, X, Y, Z);

        updateMovie(movieName, x, y, oscarsCount, genre, mpaarating);

    }

    @Override
    public boolean isMovieIDOwnedByUser(Integer id, String login) {
        try {
            String ownerLogin = findOwnerByID(id);
            if (Objects.equals(ownerLogin, login)) {
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    @Override
    public ArrayList<Integer> clearCollectionForUser(String login) throws SQLException {
        ArrayList<Integer> deletedMovieid = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(
                "SELECT id FROM movie " +
                        "JOIN clientmovie mo on movie.id = mo.movie " +
                        "WHERE client = ?");
        ps.setString(1, login);
        ResultSet resultSet = ps.executeQuery();
        while (resultSet.next()) {
            deletedMovieid.add(resultSet.getInt("id"));
        }


        ps = connection.prepareStatement(
                "DELETE FROM movie WHERE id IN (" +
                        "SELECT id FROM movie " +
                        "JOIN clientmovie mo on movie.id = mo.movie " +
                        "WHERE client = ?)");
        ps.setString(1, login);
        ps.executeUpdate();

        return deletedMovieid;
    }



    private Movie resultSetToMovie(ResultSet resultSet) throws SQLException, WrongArgumentException {

        Integer id = resultSet.getInt("id");
        String name = resultSet.getString("name");

        Coordinates coordinates = resultSetToCoordinates(resultSet);

        Instant instant = resultSet.getTimestamp("creationDate").toInstant();
        ZonedDateTime creationDate = instant.atZone(ZoneId.systemDefault());
        long oscarsCount = resultSet.getLong("oscarsCount");
        MovieGenre genre = MovieGenre.valueOf(resultSet.getString("genre"));
        MpaaRating mpaaRating = MpaaRating.valueOf(resultSet.getString("mpaaRating"));

        Person director = resultSetToPerson(resultSet);

        Movie movie = new Movie(name, coordinates, oscarsCount, genre, mpaaRating, director);
        movie.setID(id);
        movie.setCreationDate(LocalDate.from(creationDate));

        return movie;
    }

    private Person resultSetToPerson(ResultSet resultSet) throws SQLException {
        String operatorName = resultSet.getString("operatorName");
        Integer weight = resultSet.getInt("weight");
        Integer age = resultSet.getInt("age");
        Color eyecolor = Color.valueOf(resultSet.getString("eyecolor"));
        Color haircolor = Color.valueOf(resultSet.getString("haircolor"));
        Country nationality = Country.valueOf(resultSet.getString("nationality"));
        Double X = resultSet.getDouble("x");
        Float Y = resultSet.getFloat("y");
        Integer Z = resultSet.getInt("z");
        Location location = new Location(X, Y, Z);
        Person operator = new Person(operatorName, weight, age,eyecolor, haircolor, nationality, location);
        return operator;
    }

    private Coordinates resultSetToCoordinates(ResultSet resultSet) throws SQLException {
        Double x = resultSet.getDouble("x");
        Float y = resultSet.getFloat("y");
        Coordinates coordinates = new Coordinates(x, y);
        return coordinates;
    }
    private String findOwnerByID(Integer id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT client FROM clientmovie WHERE movie = ?");

        ps.setInt(1, id);

        ResultSet resultSet = ps.executeQuery();
        String login = null;
        if (resultSet.next()) {
            login = resultSet.getString("ownerLogin");
        }

        return login;
    }
    public int addMovie (String name, Double coordinatesX, Float coordinatesY, long oscarsCount, MovieGenre genre,
                         MpaaRating mpaaRating, int operator) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO movie (name, x, y, oscarscount, genre, mpaarating, operator) " +
                        "VALUES (?, ?, ?, ?, CAST(? AS moviegenre), CAST(? AS mpaarating), ?) RETURNING id");

        ps.setString(1, name);
        ps.setDouble(2, coordinatesX);
        ps.setFloat(3, coordinatesY);
        ps.setLong(4, oscarsCount);
        ps.setString(5, genre.name());
        ps.setString(6, mpaaRating.name());
        ps.setInt(7, operator);

        ResultSet resultSet = ps.executeQuery();
        int movieID = -1;
        if (resultSet.next()) {
            movieID = resultSet.getInt(1);
        }

        return movieID;
    }
    private void updateMovie(String name, Double coordinatesX, Float coordinatesY,long oscarsCount, MovieGenre genre,
                             MpaaRating mpaaRating) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "UPDATE movie SET (name, x, y, oscarscount, genre, mpaarating) = ( ?, ?, ?, ?, CAST(? AS moviegenre), CAST(? AS mpaarating)) RETURNING id");

        ps.setString(1, name);
        ps.setDouble(2, coordinatesX);
        ps.setFloat(3, coordinatesY);
        ps.setLong(4, oscarsCount);
        ps.setString(5, genre.name());
        ps.setString(6, mpaaRating.name());


        ps.executeUpdate();
    }
    private int addPerson(String name, Integer weight, Integer age, Color eyecolor, Color haircolor, Country nationality, Double X, Float Y, Integer Z)
            throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO person (name, weight, age, eyecolor, haircolor, nationality, x, y, z) VALUES (?, ?, ?, CAST(? AS color), CAST(? AS color), CAST(? AS country), ?, ?, ?) RETURNING id");

        ps.setString(1, name);
        ps.setInt(2, weight);
        ps.setInt(3, age);
        ps.setString(4, eyecolor.toString());
        ps.setString(5, haircolor.toString());
        ps.setString(6, nationality.toString());
        ps.setDouble(7, X);
        ps.setFloat(8, Y);
        ps.setInt(9, Z);


        ResultSet resultSet = ps.executeQuery();
        int id = -1;
        if (resultSet.next()) {
            id = resultSet.getInt(1);
        }

        return id;
    }
    private void updatePerson(Integer id, String name, Integer weight, Integer age, Color eyecolor, Color haircolor, Country nationality, Double X, Float Y, Integer Z)
            throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "UPDATE person SET (name, weight, age, eyecolor, haircolor, nationality, x, y, z) = (?, ?, ?, CAST(? AS color), CAST(? AS color), CAST(? AS country), ?, ?, ?) WHERE id = ?");

        ps.setString(1, name);
        ps.setInt(2, weight);
        ps.setInt(3, age);
        ps.setString(4, eyecolor.toString());
        ps.setString(5, haircolor.toString());
        ps.setString(6, nationality.toString());
        ps.setDouble(7, X);
        ps.setFloat(8, Y);
        ps.setInt(9, Z);
        ps.setInt(10, id);

        ps.executeUpdate();
    }
    private int findOperatorIDByMovie(Integer key) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT operator FROM movie WHERE id = ?");

        ps.setInt(1, key);

        ResultSet resultSet = ps.executeQuery();
        int operator = -1;
        if (resultSet.next()) {
            operator = resultSet.getInt("operator");
        }

        return operator;
    }
    private void addOwner(int movie, String login) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO clientmovie (client, movie) VALUES (?, ?)");

        ps.setInt(1, movie);
        ps.setString(2, login);

        ps.executeUpdate();
    }

}
