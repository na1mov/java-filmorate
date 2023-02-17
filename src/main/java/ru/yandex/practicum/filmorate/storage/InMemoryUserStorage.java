package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int userId = 1;

    @Override
    public User create(User user) {
        checkUserName(user);
        user.setId(userId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        userExistenceCheck(user.getId());

        checkUserName(user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User delete(Integer userId) {
        userExistenceCheck(userId);
        return users.remove(userId);
    }

    @Override
    public User getUser(Integer userId) {
        userExistenceCheck(userId);
        return users.get(userId);
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public User addFriend(Integer userId, Integer friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

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

    @Override
    public User deleteFriend(Integer userId, Integer friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

        Set<Integer> userFriends = user.getFriends();
        if (userFriends == null || userFriends.isEmpty()) {
            throw new IncorrectParameterException(String.format("У пользователя с ID:%d нет друзей", userId));
        }

        userFriends.remove(friendId);
        friend.getFriends().remove(userId);
        return user;
    }

    @Override
    public List<User> getFriends(Integer userId) {
        Set<Integer> userFriends = getUser(userId).getFriends();
        if (userFriends == null || userFriends.isEmpty()) {
            return new ArrayList<>();
        }

        return userFriends.stream()
                .map(this::getUser)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        Set<Integer> userFriends = getUser(userId).getFriends();
        if (userFriends == null || userFriends.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Integer> otherUserFriends = getUser(otherUserId).getFriends();
        if (otherUserFriends == null || otherUserFriends.isEmpty()) {
            return new ArrayList<>();
        }

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(this::getUser)
                .collect(Collectors.toList());
    }

    private void checkUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private void userExistenceCheck(Integer userId) {
        if (!users.containsKey(userId)) {
            throw new ValidationException(String.format("Пользователя с ID:%d нет в базе.", userId));
        }
    }
}
