package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        User updUser = userStorage.update(user);
        userNullCheck(updUser, user.getId());
        return updUser;
    }

    public User delete(Integer userId) {
        return userStorage.delete(userId);
    }

    public User getUser(Integer userId) {
        User user = userStorage.getUser(userId);
        userNullCheck(user, userId);
        return user;
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User addFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUser(userId);
        userNullCheck(user, userId);
        User friend = userStorage.getUser(friendId);
        userNullCheck(friend, friendId);

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
        userNullCheck(user, userId);
        User friend = userStorage.getUser(friendId);
        userNullCheck(friend, friendId);

        Set<Integer> userFriends = user.getFriends();
        if (userFriends == null || userFriends.isEmpty()) {
            throw new NotFoundException(String.format("У пользователя с ID:%d нет друзей", userId));
        }

        userFriends.remove(friendId);
        friend.getFriends().remove(userId);
        return user;
    }

    public List<User> getFriends(Integer userId) {
        User user = userStorage.getUser(userId);
        userNullCheck(user, userId);

        Set<Integer> userFriends = user.getFriends();
        if (userFriends == null || userFriends.isEmpty()) {
            return new ArrayList<>();
        }

        return userFriends.stream()
                .map(this::getUser)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        User user = userStorage.getUser(userId);
        userNullCheck(user, userId);
        User otherUser = userStorage.getUser(otherUserId);
        userNullCheck(otherUser, otherUserId);

        Set<Integer> userFriends = user.getFriends();
        if (userFriends == null || userFriends.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Integer> otherUserFriends = otherUser.getFriends();
        if (otherUserFriends == null || otherUserFriends.isEmpty()) {
            return new ArrayList<>();
        }

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(userStorage::getUser)
                .collect(Collectors.toList());
    }

    private void userNullCheck(User user, Integer userId) {
        if (user == null) {
            throw new NotFoundException(String.format("Пользователя с ID:%d нет в базе.", userId));
        }
    }
}
