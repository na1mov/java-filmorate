package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DirectorDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Director create(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("director")
                .usingGeneratedKeyColumns("director_id");

        director.setId(simpleJdbcInsert.executeAndReturnKey(directorToMap(director)).intValue());
        return director;
    }

    public Director update(Director director) {
        String sqlQuery = "UPDATE director SET name = ? WHERE director_id = ?";
        int result = jdbcTemplate.update(sqlQuery
                , director.getName()
                , director.getId());
        if (result > 0) {
            return director;
        } else {
            throw new NotFoundException(String.format("Режиссера с ID:%d нет в базе.", director.getId()));
        }
    }

    public Director delete(Integer directorId) {
        Director director = getDirector(directorId);
        String sqlQuery = "DELETE FROM director WHERE director_id = ?";
        if (jdbcTemplate.update(sqlQuery, directorId) > 0) {
            return director;
        } else {
            throw new NotFoundException(String.format("Режиссера с ID:%d нет в базе.", directorId));
        }
    }

    public Director getDirector(Integer directorId) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM director WHERE director_id = ?",
                    this::mapRowToDirector, directorId);
        } catch (Exception e) {
            throw new NotFoundException(String.format("Режиссера с ID:%d нет в базе.", directorId));
        }
    }

    public Collection<Director> getDirectors() {
        String sqlQuery = "SELECT * FROM director";
        return jdbcTemplate.query(sqlQuery, this::mapRowToDirector);
    }

    public Film getFilmDirectors(Film film) {
        String sqlQuery = "SELECT director_id, name FROM director WHERE director_id IN(" +
                "SELECT director_id FROM film_director WHERE film_id = ?)";
        film.setDirectors(new ArrayList<>(jdbcTemplate.query(sqlQuery, this::mapRowToDirector, film.getId())));
        return film;
    }

    public List<Film> getFilmsDirectors(List<Film> films) {
        List<Integer> filmsId = films.stream().map(Film::getId).collect(Collectors.toList());
        Map<Integer, Film> idToFilm = new LinkedHashMap<>();
        for (Film film : films) {
            idToFilm.put(film.getId(), film);
        }

        SqlParameterSource param = new MapSqlParameterSource("filmsId", filmsId);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

        String sqlQuery = "SELECT * FROM film_director AS f " +
                "INNER JOIN director AS dir ON dir.director_id = f.director_id " +
                "WHERE film_id IN(:filmsId)";

        namedJdbcTemplate.query(sqlQuery, param,
                (resultSet, rowNum) -> idToFilm.get(resultSet.getInt("film_id")).getDirectors()
                        .add(mapRowToDirector(resultSet, rowNum)));

        return new ArrayList<>(idToFilm.values());
    }

    public Film updateFilmDirectors(Film film) {
        String sqlQueryDel = "DELETE FROM film_director WHERE film_id = ?";
        jdbcTemplate.update(sqlQueryDel, film.getId());

        return addFilmDirectors(film);
    }

    public Film addFilmDirectors(Film film) {
        if (film.getDirectors() == null) {
            return film;
        }

        Integer filmId = film.getId();
        List<Director> directors = new ArrayList<>(film.getDirectors());
        String sqlQuery = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";

        BatchPreparedStatementSetter batchPreparedStatementSetter = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Director director = directors.get(i);
                ps.setInt(1, filmId);
                ps.setInt(2, director.getId());
            }

            @Override
            public int getBatchSize() {
                return directors.size();
            }
        };

        jdbcTemplate.batchUpdate(sqlQuery, batchPreparedStatementSetter);
        return film;
    }

    private Director mapRowToDirector(ResultSet resultSet, int rowNum) throws SQLException {
        return Director.builder()
                .id(resultSet.getInt("director_id"))
                .name(resultSet.getString("name"))
                .build();
    }

    private Map<String, Object> directorToMap(Director director) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", director.getName());
        return values;
    }
}
