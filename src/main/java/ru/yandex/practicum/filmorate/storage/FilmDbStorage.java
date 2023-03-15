package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("film")
                .usingGeneratedKeyColumns("film_id");

        film.setId(simpleJdbcInsert.executeAndReturnKey(filmToMap(film)).intValue());
        return film;
    }

    @Override
    public Film update(Film film) {
        String sqlQueryUpdFilm = "UPDATE film SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, rating_mpa_id = ? WHERE film_id = ?";
        int updateResult = jdbcTemplate.update(sqlQueryUpdFilm
                , film.getName()
                , film.getDescription()
                , film.getReleaseDate()
                , film.getDuration()
                , film.getMpa().getId()
                , film.getId());
        if (updateResult > 0) {
            return film;
        } else {
            throw new NotFoundException(String.format("Фильма с ID:%d нет в базе.", film.getId()));
        }
    }

    @Override
    public Film delete(Integer filmId) {
        Film film = getFilm(filmId);
        String sqlQuery = "DELETE FROM film WHERE film_id = ?";
        if (jdbcTemplate.update(sqlQuery, filmId) > 0) {
            return film;
        } else {
            throw new NotFoundException(String.format("Фильма с ID:%d нет в базе.", filmId));
        }
    }

    @Override
    public Film getFilm(Integer filmId) {
        String sqlQuery = "SELECT f.*, rm.name AS rm_name FROM film AS f " +
                "LEFT JOIN rating_mpa AS rm ON f.rating_mpa_id = rm.rating_mpa_id " +
                "WHERE f.film_id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, filmId);
        } catch (Exception e) {
            throw new NotFoundException(String.format("Фильма с ID:%d нет в базе.", filmId));
        }
    }

    @Override
    public List<Film> getFilms() {
        String sqlQuery = "SELECT f.*, rm.name AS rm_name " +
                "FROM film AS f " +
                "LEFT JOIN rating_mpa AS rm ON f.rating_mpa_id = rm.rating_mpa_id ";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
    }

    @Override
    public Film addLike(Integer filmId, Integer userId) {
        Film film = getFilm(filmId);
        String sqlQuery = "INSERT INTO film_like (film_id, user_id) VALUES(?, ?)";
        jdbcTemplate.update(sqlQuery, filmId, userId);
        return film;
    }

    @Override
    public Film removeLike(Integer filmId, Integer userId) {
        Film film = getFilm(filmId);
        String sqlQuery = "DELETE FROM film_like WHERE film_id = ? AND user_id = ?";
        if (jdbcTemplate.update(sqlQuery, filmId, userId) > 0) {
            return film;
        } else {
            throw new NotFoundException(String.format("У фильма с ID:%d нет лайка от пользователя с ID%d.",
                    filmId, userId));
        }
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        String sqlQuery = "SELECT f.*, rm.name AS rm_name, COUNT(fl.film_id) AS like_count " +
                "FROM film AS f " +
                "LEFT JOIN rating_mpa AS rm ON f.rating_mpa_id = rm.rating_mpa_id " +
                "LEFT JOIN film_like AS fl ON f.film_id = fl.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY like_count DESC " +
                "LIMIT ?";
        List<Film> result = jdbcTemplate.query(sqlQuery, this::mapRowToFilm, count);
        if (result.size() != 0) {
            return result;
        } else {
            return new ArrayList<>();
        }
    }

    public Collection<Integer> getFilmLikes(Integer filmId) {
        String sqlQuery = "SELECT user_id FROM film_like WHERE film_id = ?";
        List<Integer> result = jdbcTemplate.queryForList(sqlQuery, Integer.class, filmId);
        if (result.size() != 0) {
            return result;
        } else {
            return new ArrayList<>();
        }
    }

    public List<Film> getDirectorFilms(Integer directorId, String sortBy) {
        String sort;
        switch (sortBy) {
            case "year":
                sort = "ORDER BY f.release_date, f.name";
                break;
            case "likes":
                sort = "ORDER BY like_count DESC, f.name";
                break;
            default:
                sort = "ORDER BY f.name";
        }

        String sqlQuery = "SELECT f.*, rm.name AS rm_name, COUNT(fl.film_id) AS like_count " +
                "FROM film AS f " +
                "LEFT JOIN rating_mpa AS rm ON f.rating_mpa_id = rm.rating_mpa_id " +
                "LEFT JOIN film_like AS fl ON f.film_id = fl.film_id " +
                "WHERE f.film_id IN(SELECT film_id FROM film_director WHERE director_id = ?)" +
                "GROUP BY f.film_id " +
                sort;

        List<Film> result = jdbcTemplate.query(sqlQuery, this::mapRowToFilm, directorId);

        if (result.size() != 0) {
            return result;
        } else {
            return new ArrayList<>();
        }
    }

    private Map<String, Object> filmToMap(Film film) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        values.put("rating_mpa_id", film.getMpa().getId());
        return values;
    }

    private Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(resultSet.getInt("film_id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .build();
        film.setMpa(MpaRating.builder()
                .id(resultSet.getInt("rating_mpa_id"))
                .name(resultSet.getString("rm_name"))
                .build());
        film.setDirectors(new ArrayList<>());
        film.setGenres(new HashSet<>());
        film.setLikes(new HashSet<>(getFilmLikes(film.getId())));
        return film;
    }
}
