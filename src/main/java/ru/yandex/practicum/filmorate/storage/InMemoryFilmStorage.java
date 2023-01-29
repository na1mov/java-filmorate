package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

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
    public Collection<Film> getFilms() {
        return films.values();
    }

    private void filmExistenceCheck(Integer filmId) {
        if (!films.containsKey(filmId)) {
            throw new NotFoundException(String.format("Фильма с ID:%d нет в базе.", filmId));
        }
    }
}
