package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Service
public class UserService {
    private final Map<Integer, User> users = new HashMap<>();
    private int userId = 1;

    public Collection<User> getUsers() {
        return users.values();
    }

    public User createUser(User user) {
        checkUser(user);

        user.setId(userId++);
        users.put(user.getId(), user);
        return user;
    }

    public User updateUser(User user) {
        checkUser(user);
        if (!users.containsKey(user.getId())) {
            throw new ValidationException(String.format("Пользователя с ID:%d нет в базе.", user.getId()));
        }
        users.put(user.getId(), user);
        return user;
    }

    private void checkUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым или содержать пробелы.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
    }
}
