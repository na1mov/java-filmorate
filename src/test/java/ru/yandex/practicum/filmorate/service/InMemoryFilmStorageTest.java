package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;

class InMemoryFilmStorageTest {
    private static InMemoryFilmStorage inMemoryFilmStorage;

    @BeforeEach
    public void beforeEach() {
        inMemoryFilmStorage = new InMemoryFilmStorage();
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
        Assertions.assertThrows(IncorrectParameterException.class, () -> inMemoryFilmStorage.update(film));
    }
}