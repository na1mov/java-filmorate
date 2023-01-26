package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> getFilms();

    Film create(Film film);

    Film update(Film film);

    Film getFilm(Integer filmId);
}
