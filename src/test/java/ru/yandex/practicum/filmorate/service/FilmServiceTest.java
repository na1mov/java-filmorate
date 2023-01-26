package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

class FilmServiceTest {
    private static FilmService filmService;

    @BeforeEach
    public void beforeEach() {
        filmService = new FilmService();
    }

    // тест update()
    @Test
    public void shouldThrowValidationExceptionIfUserIdIsWrong() {
        Film film = Film.builder()
                .id(5)
                .name("FilmName")
                .description("Description")
                .releaseDate(LocalDate.of(1999, 9, 9))
                .duration(99)
                .build();
        Assertions.assertThrows(ValidationException.class, () -> filmService.update(film));
    }
}