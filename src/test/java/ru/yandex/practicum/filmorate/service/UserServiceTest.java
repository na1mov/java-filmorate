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
        User user2 = new User("user2 mail.ru", "user2", LocalDate.of(1991, 6, 28));
        user2.setId(5);
        Assertions.assertThrows(ValidationException.class, () -> userService.updateUser(user2));
    }

    // тесты checkUser()
    @Test
    public void shouldThrowValidationExceptionIfEmailIsWrong() {
        User user2 = new User("user2 mail.ru", "user2", LocalDate.of(1991, 6, 28));
        Assertions.assertThrows(ValidationException.class, () -> userService.createUser(user2));
    }

    @Test
    public void shouldThrowValidationExceptionIfEmailIsBlank() {
        User user2 = new User(" ", "user2", LocalDate.of(1991, 6, 28));
        Assertions.assertThrows(ValidationException.class, () -> userService.createUser(user2));
    }

    @Test
    public void shouldThrowValidationExceptionIfLoginIsBlank() {
        User user2 = new User("user2@mail.ru", "", LocalDate.of(1991, 6, 28));
        Assertions.assertThrows(ValidationException.class, () -> userService.createUser(user2));
    }

    @Test
    public void shouldThrowValidationExceptionIfLoginContainsWhitespace() {
        User user2 = new User("user2@mail.ru", "user 2", LocalDate.of(1991, 6, 28));
        Assertions.assertThrows(ValidationException.class, () -> userService.createUser(user2));
    }

    @Test
    public void shouldThrowValidationExceptionIfBirthdayInFuture() {
        User user2 = new User("user2@mail.ru", "user2", LocalDate.of(2991, 6, 28));
        Assertions.assertThrows(ValidationException.class, () -> userService.createUser(user2));
    }

    @Test
    public void shouldSetNameIfItIsEmptyUsingLoginInfo() {
        User user2 = new User("user2@mail.ru", "user2", LocalDate.of(1991, 6, 28));
        user2.setName("");
        userService.createUser(user2);
        Assertions.assertEquals("user2", user2.getName());
    }
}