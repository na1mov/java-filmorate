package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Service
public class UserService {
    private final Map<Integer, User> users = new HashMap<>();
    private int userId = 1;

    public Collection<User> getUsers() {
        return users.values();
    }

    public User createUser(User user) {
        checkUserName(user);
        user.setId(userId++);
        users.put(user.getId(), user);
        return user;
    }

    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new ValidationException(String.format("Пользователя с ID:%d нет в базе.", user.getId()));
        }

        checkUserName(user);
        users.put(user.getId(), user);
        return user;
    }

    private void checkUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
