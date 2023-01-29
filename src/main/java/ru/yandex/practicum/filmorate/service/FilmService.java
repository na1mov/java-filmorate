package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        Film updFilm = filmStorage.update(film);
        filmNullCheck(updFilm, film.getId());
        return updFilm;
    }

    public Film delete(Integer filmId) {
        return filmStorage.delete(filmId);
    }

    public Film getFilm(Integer filmId) {
        Film film = filmStorage.getFilm(filmId);
        filmNullCheck(film, filmId);
        return film;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilm(filmId);
        filmNullCheck(film, filmId);
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        film.getLikes().add(userId);
        return film;
    }

    public Film removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilm(filmId);
        filmNullCheck(film, filmId);
        if (film.getLikes() == null) {
            throw new NotFoundException(String.format("У фильма с ID:%d нет лайков", filmId));
        }
        film.getLikes().remove(userId);
        return film;
    }

    public List<Film> getPopularFilms(Integer count) {
        Collection<Film> films = filmStorage.getFilms();
        if (films == null || films.isEmpty()) {
            return new ArrayList<>();
        }
        return filmStorage.getFilms().stream()
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

    private void filmNullCheck(Film film, Integer filmId) {
        if (film == null) {
            throw new NotFoundException(String.format("Фильма с ID:%d нет в базе.", filmId));
        }
    }
}
