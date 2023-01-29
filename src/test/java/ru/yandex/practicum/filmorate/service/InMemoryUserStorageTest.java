package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

class InMemoryUserStorageTest {
    private static InMemoryUserStorage inMemoryUserStorage;

    @BeforeEach
    public void beforeEach() {
        inMemoryUserStorage = new InMemoryUserStorage();
    }

    // тест updateUser()
    @Test
    public void shouldThrowValidationExceptionIfUserIdIsWrong() {
        User user2 = User.builder()
                .id(5)
                .email("user2@mail.ru")
                .login("user2")
                .birthday(LocalDate.of(1991, 6, 28))
                .build();
        Assertions.assertThrows(IncorrectParameterException.class, () -> inMemoryUserStorage.update(user2));
    }

    // тесты checkUserName()
    @Test
    public void shouldSetNameIfItIsEmptyUsingLoginInfo() {
        User user2 = User.builder()
                .email("user2@mail.ru")
                .login("user2")
                .birthday(LocalDate.of(1991, 6, 28))
                .build();
        inMemoryUserStorage.create(user2);
        Assertions.assertEquals("user2", user2.getName());
    }
}