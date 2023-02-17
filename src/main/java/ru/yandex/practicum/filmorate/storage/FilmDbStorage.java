package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingDbStorage mpaRatingDbStorage;
    private final GenreDbStorage genreDbStorage;

    @Override
    public Film create(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("film")
                .usingGeneratedKeyColumns("film_id");

        film.setId(simpleJdbcInsert.executeAndReturnKey(filmToMap(film)).intValue());
        addFilmGenres(film);
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
            updateFilmGenres(film);
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
        String sqlQuery = "SELECT * FROM film WHERE film_id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToFilm, filmId);
        } catch (Exception e) {
            throw new NotFoundException(String.format("Фильма с ID:%d нет в базе.", filmId));
        }
    }

    @Override
    public Collection<Film> getFilms() {
        String sqlQuery = "SELECT * FROM film";
        return jdbcTemplate.query(sqlQuery, this::mapRowToFilm);
    }

    @Override
    public Film addLike(Integer filmId, Integer userId) {
        String sqlQuery = "INSERT INTO film_like (film_id, user_id) VALUES(?, ?)";
        if (jdbcTemplate.update(sqlQuery, filmId, userId) > 0) {
            return getFilm(filmId);
        } else {
            throw new NotFoundException(String.format("Фильма с ID:%d нет в базе.", filmId));
        }
    }

    @Override
    public Film removeLike(Integer filmId, Integer userId) {
        String sqlQuery = "DELETE FROM film_like WHERE film_id = ? AND user_id = ?";
        if (jdbcTemplate.update(sqlQuery, filmId, userId) > 0) {
            return getFilm(filmId);
        } else {
            throw new NotFoundException(String.format("Фильма с ID:%d нет в базе.", filmId));
        }
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        String sqlQuery = "SELECT f.film_id, f.name, f.description, f.release_date, " +
                "f.duration, f.rating_mpa_id, fl.like_count " +
                "FROM film AS f " +
                "LEFT JOIN (SELECT film_id, COUNT(user_id) AS like_count " +
                "FROM film_like GROUP BY film_id) AS fl ON f.film_id = fl.film_id " +
                "ORDER BY fl.like_count DESC LIMIT ?";
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

    public Collection<Genre> getFilmGenres(Integer filmId) {
        String sqlQuery = "SELECT genre_id FROM film_genre WHERE film_id = ?";
        Collection<Integer> listInteger = jdbcTemplate.queryForList(sqlQuery, Integer.class, filmId);
        List<Genre> result = listInteger.stream()
                .map(genreDbStorage::getGenre)
                .collect(Collectors.toList());
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
        film.setMpa(mpaRatingDbStorage.getMpaRating(resultSet.getInt("rating_mpa_id")));
        film.setLikes(new HashSet<>(getFilmLikes(film.getId())));
        film.setGenres(new HashSet<>(getFilmGenres(film.getId())));
        return film;
    }

    private void updateFilmGenres(Film film) {
        String sqlQueryDel = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(sqlQueryDel, film.getId());

        if (film.getGenres() != null) {
            String sqlQueryUpd = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            film.getGenres().stream()
                    .map(Genre::getId)
                    .forEach(genreId -> jdbcTemplate.update(sqlQueryUpd, film.getId(), genreId));
        }
    }

    private void addFilmGenres(Film film) {
        if (film.getGenres() == null) {
            return;
        }

        String sqlQuery = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        film.getGenres()
                .stream()
                .map(Genre::getId)
                .forEach(genreId -> jdbcTemplate.update(sqlQuery, film.getId(), genreId));
    }
}
