package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private static FilmService filmService;

    @BeforeEach
    public void beforeEach() {
        filmService = new FilmService();
    }

    // тест update()
    @Test
    public void shouldThrowValidationExceptionIfUserIdIsWrong() {
        Film film = new Film("FilmName", "Description",
                LocalDate.of(1999, 9, 9), 99);
        film.setId(5);
        Assertions.assertThrows(ValidationException.class, () -> filmService.update(film));
    }

    // тесты checkFilm()
    @Test
    public void shouldThrowValidationExceptionIfNameIsBlank() {
        Film film = new Film("", "Description",
                LocalDate.of(1999, 9, 9), 99);
        Assertions.assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    @Test
    public void shouldThrowValidationExceptionIfDescriptionSizeIsMoreThan200() {
        Film film = new Film("FilmName", "Description size should not be more than 200 symbols. " +
                "This message contains 101 symbols. And doubled. Description size should not be more than " +
                "200 symbols. This message contains 101 symbols. And doubled.",
                LocalDate.of(1999, 9, 9), 99);
        Assertions.assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    @Test
    public void shouldThrowValidationExceptionIfReleaseDateIsBefore1895() {
        Film film = new Film("FilmName", "Description",
                LocalDate.of(1555, 5, 5), 55);
        Assertions.assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    @Test
    public void shouldThrowValidationExceptionIfFilmDurationIsNegative() {
        Film film = new Film("FilmName", "Description",
                LocalDate.of(1999, 9, 9), -1);
        Assertions.assertThrows(ValidationException.class, () -> filmService.create(film));
    }
}