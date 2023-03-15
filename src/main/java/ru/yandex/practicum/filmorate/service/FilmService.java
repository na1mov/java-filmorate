package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreDbStorage;
    private final DirectorDbStorage directorDbStorage;

    public Film create(Film film) {
        Film result = genreDbStorage.addFilmGenres(filmStorage.create(film));
        return directorDbStorage.addFilmDirectors(result);
    }

    public Film update(Film film) {
        Film result = genreDbStorage.updateFilmGenres(filmStorage.update(film));
        return directorDbStorage.updateFilmDirectors(result);
    }

    public Film delete(Integer filmId) {
        return filmStorage.delete(filmId);
    }

    public Film getFilm(Integer filmId) {
        Film result = genreDbStorage.getFilmGenres(filmStorage.getFilm(filmId));
        return directorDbStorage.getFilmDirectors(result);
    }

    public Collection<Film> getFilms() {
        List<Film> result = genreDbStorage.getFilmsGenres(filmStorage.getFilms());
        return directorDbStorage.getFilmsDirectors(result);
    }

    public Film addLike(Integer filmId, Integer userId) {
        userStorage.getUser(userId);
        return filmStorage.addLike(filmId, userId);
    }

    public Film removeLike(Integer filmId, Integer userId) {
        userStorage.getUser(userId);
        return filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(Integer count) {
        List<Film> result = genreDbStorage.getFilmsGenres(filmStorage.getPopularFilms(count));
        return directorDbStorage.getFilmsDirectors(result);
    }

    public List<Film> getDirectorFilms(Integer directorId, String sortBy) {
//        if (!(sortBy.equals("year".toLowerCase()) || sortBy.equals("likes".toLowerCase()))) {
//            throw new IncorrectParameterException("Значение параметра sortBy должно быть \"year\" или \"likes\"");
//        }
        Director director = directorDbStorage.getDirector(directorId);

        List<Film> films = genreDbStorage.getFilmsGenres(filmStorage.getDirectorFilms(directorId,
                sortBy.toLowerCase()));
        films = directorDbStorage.getFilmsDirectors(films);
//        if (films.size() == 0) {
//            throw new NotFoundException("Фильмов от этого режиссёра не найдено.");
//        }
        return films;
    }
}
