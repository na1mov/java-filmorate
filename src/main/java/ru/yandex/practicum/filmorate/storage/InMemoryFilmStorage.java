package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
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
        if (!films.containsKey(film.getId())) {
            return null;
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film delete(Integer filmId) {
        if (!films.containsKey(filmId)) {
            return null;
        }
        return films.remove(filmId);
    }

    @Override
    public Film getFilm(Integer filmId) {
        if (!films.containsKey(filmId)) {
            return null;
        }
        return films.get(filmId);
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }
}
