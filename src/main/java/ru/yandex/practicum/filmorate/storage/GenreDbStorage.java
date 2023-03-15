package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Genre getGenre(Integer genreId) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM genre WHERE genre_id = ?",
                    this::mapRowToGenre, genreId);
        } catch (Exception e) {
            throw new NotFoundException(String.format("Жанра с ID:%d нет в базе.", genreId));
        }
    }

    public Collection<Genre> getGenres() {
        String sqlQuery = "SELECT * FROM genre";
        return jdbcTemplate.query(sqlQuery, this::mapRowToGenre);
    }

    public Film getFilmGenres(Film film) {
        String sqlQuery = "SELECT genre_id, name FROM genre WHERE genre_id IN(" +
                "SELECT genre_id FROM film_genre WHERE film_id = ?)";
        film.setGenres(new HashSet<>(jdbcTemplate.query(sqlQuery, this::mapRowToGenre, film.getId())));
        return film;
    }

    public List<Film> getFilmsGenres(List<Film> films) {
        List<Integer> filmsId = films.stream().map(Film::getId).collect(Collectors.toList());
        Map<Integer, Film> idToFilm = new LinkedHashMap<>();
        for (Film film : films) {
            idToFilm.put(film.getId(), film);
        }

        SqlParameterSource param = new MapSqlParameterSource("filmsId", filmsId);
        NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

        String sqlQuery = "SELECT * FROM film_genre AS f " +
                "INNER JOIN genre AS g ON g.genre_id = f.genre_id " +
                "WHERE film_id IN(:filmsId)";

        namedJdbcTemplate.query(sqlQuery, param,
                (resultSet, rowNum) -> idToFilm.get(resultSet.getInt("film_id")).getGenres()
                        .add(mapRowToGenre(resultSet, rowNum)));

        return new ArrayList<>(idToFilm.values());
    }

    public Film updateFilmGenres(Film film) {
        String sqlQueryDel = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(sqlQueryDel, film.getId());

        return addFilmGenres(film);
    }

    public Film addFilmGenres(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
            return film;
        }

        Integer filmId = film.getId();
        List<Genre> genres = new ArrayList<>(film.getGenres());
        String sqlQuery = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

        BatchPreparedStatementSetter batchPreparedStatementSetter = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Genre genre = genres.get(i);
                ps.setInt(1, filmId);
                ps.setInt(2, genre.getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        };

        jdbcTemplate.batchUpdate(sqlQuery, batchPreparedStatementSetter);
        return film;
    }

    private Genre mapRowToGenre(ResultSet resultSet, int rowNum) throws SQLException {
        return Genre.builder()
                .id(resultSet.getInt("genre_id"))
                .name(resultSet.getString("name"))
                .build();
    }
}
