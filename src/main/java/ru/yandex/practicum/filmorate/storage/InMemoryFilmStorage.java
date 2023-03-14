package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int filmId = 1;

    @Override
    public Film create(Film film) {
        film.setId(filmId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        filmExistenceCheck(film.getId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film delete(Integer filmId) {
        filmExistenceCheck(filmId);
        return films.remove(filmId);
    }

    @Override
    public Film getFilm(Integer filmId) {
        filmExistenceCheck(filmId);
        return films.get(filmId);
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film addLike(Integer filmId, Integer userId) {
        Film film = getFilm(filmId);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        film.getLikes().add(userId);
        return film;
    }

    @Override
    public Film removeLike(Integer filmId, Integer userId) {
        Film film = getFilm(filmId);
        if (film.getLikes() == null) {
            throw new ValidationException(String.format("У фильма с ID:%d нет лайков", filmId));
        }
        film.getLikes().remove(userId);
        return film;
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        Collection<Film> films = getFilms();
        if (films == null || films.isEmpty()) {
            return new ArrayList<>();
        }
        return getFilms().stream()
                .sorted(this::compare)
                .limit(count)
                .collect(Collectors.toList());
    }

    private int compare(Film f0, Film f1) {
        if (f0.getLikes() == null || f0.getLikes().isEmpty()) {
            if (f1.getLikes() == null || f1.getLikes().isEmpty()) {
                return 0;
            }
            return 1;
        }
        if (f1.getLikes() == null || f1.getLikes().isEmpty()) {
            return -1;
        }
        return f1.getLikes().size() - f0.getLikes().size();
    }

    private void filmExistenceCheck(Integer filmId) {
        if (!films.containsKey(filmId)) {
            throw new ValidationException(String.format("Фильма с ID:%d нет в базе.", filmId));
        }
    }
}
