package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int userId = 1;

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User create(User user) {
        checkUserName(user);
        user.setId(userId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new ValidationException(String.format("Пользователя с ID:%d нет в базе.", user.getId()));
        }

        checkUserName(user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUser(Integer userId) {
        if (!users.containsKey(userId)) {
            throw new ValidationException(String.format("Пользователя с ID:%d нет в базе.", userId));
        }
        return users.get(userId);
    }

    private void checkUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
