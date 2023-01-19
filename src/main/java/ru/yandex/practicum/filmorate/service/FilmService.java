package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class FilmService {
    private final Map<Integer, Film> films = new HashMap<>();
    private int filmId = 1;

    public Collection<Film> getFilms() {
        return films.values();
    }

    public Film create(Film film) {
        checkFilm(film);
        film.setId(filmId++);
        films.put(film.getId(), film);
        return film;
    }

    public Film update(Film film) {
        checkFilm(film);
        if (!films.containsKey(film.getId())) {
            throw new ValidationException(String.format("Фильма с ID:%d нет в базе.", film.getId()));
        }
        films.put(film.getId(), film);
        return film;
    }

    private void checkFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым.");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
        }
        if (film.getDuration() < 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной.");
        }
    }
}
