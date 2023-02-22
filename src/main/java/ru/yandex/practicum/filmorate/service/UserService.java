package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User delete(Integer userId) {
        return userStorage.delete(userId);
    }

    public User getUser(Integer userId) {
        return userStorage.getUser(userId);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User addFriend(Integer userId, Integer friendId) {
        return userStorage.addFriend(userId, friendId);
    }

    public User deleteFriend(Integer userId, Integer friendId) {
        return userStorage.deleteFriend(userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        return userStorage.getCommonFriends(userId, otherUserId);
    }
}
