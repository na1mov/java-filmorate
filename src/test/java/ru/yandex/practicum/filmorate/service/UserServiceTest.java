package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

class UserServiceTest {
    private static UserService userService;

    @BeforeEach
    public void beforeEach() {
        userService = new UserService();
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
        Assertions.assertThrows(ValidationException.class, () -> userService.updateUser(user2));
    }

    // тесты checkUserName()
    @Test
    public void shouldSetNameIfItIsEmptyUsingLoginInfo() {
        User user2 = User.builder()
                .email("user2@mail.ru")
                .login("user2")
                .birthday(LocalDate.of(1991, 6, 28))
                .build();
        userService.createUser(user2);
        Assertions.assertEquals("user2", user2.getName());
    }
}