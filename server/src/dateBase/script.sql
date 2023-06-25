CREATE TYPE movieGenre AS ENUM('DRAMA', 'COMEDY', 'TRAGEDY', 'THRILLER', 'SCIENCE_FICTION');
CREATE TYPE mpaaRating AS ENUM('G', 'PG', 'PG_13', 'R', 'NC_17');
CREATE TYPE color AS ENUM('RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE', 'BROWN', 'BLACK', 'WHITE');
CREATE TYPE country AS ENUM('THAILAND', 'SOUTH_KOREA', 'NORTH_KOREA', 'USA', 'RUSSIA', 'ITALY', 'FRANCE', 'JAPAN');

CREATE TABLE person(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL CHECK ( name <> '' ),
    weight INTEGER NOT NULL CHECK (weight > 0),
    eyeColor color NOT NULL,
    hairColor color NOT NULL,
    x DOUBLE PRECISION NOT NULL,
    y FLOAT NOT NULL,
    z INTEGER NOT NULL
);

CREATE TABLE movie(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL CHECK ( name <> '' ),
    x DOUBLE PRECISION NOT NULL,
    y FLOAT NOT NULL,
    creationDate TIMESTAMPTZ DEFAULT NOW() NOT NULL,
    oscarsCount INTEGER NOT NULL CHECK (oscarsCount > 0),
    genre movieGenre NOT NULL,
    operator INTEGER NOT NULL REFERENCES person
);
CREATE TABLE client(
    login INTEGER PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);
CREATE TABLE clientMovie(
    client INTEGER REFERENCES client(login),
    movie INTEGER REFERENCES movie(id),
    PRIMARY KEY (client, movie)
)