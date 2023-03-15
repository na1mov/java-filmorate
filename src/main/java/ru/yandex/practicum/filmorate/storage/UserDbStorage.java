package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        checkUserName(user);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");

        user.setId(simpleJdbcInsert.executeAndReturnKey(userToMap(user)).intValue());
        return user;
    }

    @Override
    public User update(User user) {
        checkUserName(user);
        String sqlQuery = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int result = jdbcTemplate.update(sqlQuery
                , user.getEmail()
                , user.getLogin()
                , user.getName()
                , user.getBirthday()
                , user.getId());
        if (result > 0) {
            return user;
        } else {
            throw new NotFoundException(String.format("Пользователя с ID:%d нет в базе.", user.getId()));
        }
    }

    @Override
    public User delete(Integer userId) {
        User user = getUser(userId);
        String sqlQuery = "DELETE FROM users WHERE user_id = ?";
        if (jdbcTemplate.update(sqlQuery, userId) > 0) {
            return user;
        } else {
            throw new NotFoundException(String.format("Пользователя с ID:%d нет в базе.", userId));
        }
    }

    @Override
    public User getUser(Integer userId) {
        String sqlQuery = "SELECT * FROM users WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToUser, userId);
        } catch (Exception e) {
            throw new NotFoundException(String.format("Пользователя с ID:%d нет в базе.", userId));
        }
    }

    @Override
    public Collection<User> getUsers() {
        String sqlQuery = "SELECT * FROM users";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser);
    }

    @Override
    public User addFriend(Integer userId, Integer friendId) {
        User user = getUser(userId);
        checkUserExistence(friendId);
        String sqlQuery = "INSERT INTO friendship (user_id, friend_id) VALUES(?, ?)";
        jdbcTemplate.update(sqlQuery, userId, friendId);
        return user;
    }

    @Override
    public User deleteFriend(Integer userId, Integer friendId) {
        User user = getUser(userId);
        checkUserExistence(friendId);
        String sqlQuery = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        if (jdbcTemplate.update(sqlQuery, userId, friendId) > 0) {
            return user;
        } else {
            throw new NotFoundException(String.format("Пользователи с ID:%d и ID:%d не являются друзьями.",
                    userId, friendId));
        }
    }

    @Override
    public List<User> getFriends(Integer userId) {
        checkUserExistence(userId);
        String sqlQuery = "SELECT * FROM users WHERE user_id IN(SELECT friend_id FROM friendship WHERE user_id = ?)";
        List<User> result = jdbcTemplate.query(sqlQuery, this::mapRowToUser, userId);
        if (result.size() != 0) {
            return result;
        } else {
            return new ArrayList<>();
        }
    }

    public Set<Integer> getFriendsIds(Integer userId) {
        checkUserExistence(userId);
        String sqlQuery = "SELECT friend_id FROM friendship WHERE user_id = ?";
        List<Integer> result = jdbcTemplate.queryForList(sqlQuery, Integer.class, userId);
        if (result.size() != 0) {
            return new HashSet<>(result);
        } else {
            return new HashSet<>();
        }
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer otherUserId) {
        checkUserExistence(userId);
        checkUserExistence(otherUserId);
        String sqlQuery = "SELECT * FROM users WHERE user_id IN(" +
                "SELECT friend_id FROM friendship WHERE user_id = ?) " +
                "AND user_id IN(SELECT friend_id FROM friendship WHERE user_id = ?)";
        return jdbcTemplate.query(sqlQuery, this::mapRowToUser, userId, otherUserId);
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> values = new HashMap<>();
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("name", user.getName());
        values.put("birthday", user.getBirthday());
        return values;
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        User user = User.builder()
                .id(resultSet.getInt("user_id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
        user.setFriends(getFriendsIds(user.getId()));
        return user;
    }

    private void checkUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public void checkUserExistence(Integer userId) {
        String sqlQueryExCheck = "SELECT user_id FROM users WHERE user_id = ?";
        if (jdbcTemplate.queryForList(sqlQueryExCheck, Integer.class, userId).size() == 0) {
            throw new NotFoundException(String.format("Пользователя с ID:%d нет в базе.", userId));
        }
    }
}
