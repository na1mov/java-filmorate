package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User getUser(Integer userId) {
        return userStorage.getUser(userId);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User addFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (friend.getFriends() == null) {
            friend.setFriends(new HashSet<>());
        }
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        return user;
    }

    public User deleteFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUser(userId);
        User friend = userStorage.getUser(friendId);
        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            throw new ValidationException(String.format("У пользователя с ID:%d нет друзей", userId));
        }
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        return user;
    }

    public List<User> getFriends(Integer userId) {
        User user = userStorage.getUser(userId);
        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            return new ArrayList<>();
        }
        return user.getFriends().stream()
                .map(this::getUser)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        User user = userStorage.getUser(userId);
        User otherUser = userStorage.getUser(otherUserId);
        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            return new ArrayList<>();
        }
        if (otherUser.getFriends() == null || otherUser.getFriends().isEmpty()) {
            return new ArrayList<>();
        }
        return user.getFriends().stream()
                .filter(i -> otherUser.getFriends().contains(i))
                .map(userStorage::getUser)
                .collect(Collectors.toList());
    }
}
