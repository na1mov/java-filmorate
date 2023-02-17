package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;
    private User user;

    @BeforeEach
    void beforeEach() {
        jdbcTemplate.update("DELETE FROM users");
        user = User.builder()
                .email("user@mail.ru")
                .login("user")
                .birthday(LocalDate.of(1991, 6, 28))
                .friends(new HashSet<>())
                .build();
    }

    @Test
    void shouldAddUserWithRightIdNameAndGetItBack() {
        userDbStorage.create(user);
        Assertions.assertEquals(user, userDbStorage.getUser(user.getId()));
    }

    @Test
    void shouldUpdateUserEmailAndName() {
        userDbStorage.create(user);
        user.setEmail("newEmail4Tests@mail.ru");
        user.setName("Aleksey");
        userDbStorage.update(user);
        Assertions.assertEquals("newEmail4Tests@mail.ru", userDbStorage.getUser(user.getId()).getEmail());
        Assertions.assertEquals("Aleksey", userDbStorage.getUser(user.getId()).getName());
    }

    @Test
    void shouldDeleteUserAndGetEmptyUsersList() {
        userDbStorage.create(user);
        userDbStorage.delete(user.getId());
        Assertions.assertEquals(new ArrayList<>(), userDbStorage.getUsers());
    }

    @Test
    void shouldGetUserFromDb() {
        userDbStorage.create(user);
        Assertions.assertEquals(user, userDbStorage.getUser(user.getId()));
    }

    @Test
    void shouldGetUsersWithRightListSizeAndContent() {
        userDbStorage.create(user);
        Assertions.assertEquals(1, userDbStorage.getUsers().size());
        Assertions.assertEquals(user, userDbStorage.getUsers().toArray()[0]);
    }

    @Test
    void shouldAddFriendAndGetFriendsListWithRightConditions() {
        User friend = User.builder()
                .email("friend@mail.ru")
                .login("friend")
                .birthday(LocalDate.of(1991, 4, 27))
                .friends(new HashSet<>())
                .build();
        userDbStorage.create(user);
        userDbStorage.create(friend);
        userDbStorage.addFriend(user.getId(), friend.getId());
        Assertions.assertEquals(1, userDbStorage.getFriends(user.getId()).size());
        Assertions.assertEquals(friend, userDbStorage.getFriends(user.getId()).toArray()[0]);
    }

    @Test
    void shouldDeleteFriendAndGetEmptyFriendList() {
        User friend = User.builder()
                .email("friend@mail.ru")
                .login("friend")
                .birthday(LocalDate.of(1991, 4, 27))
                .friends(new HashSet<>())
                .build();
        userDbStorage.create(user);
        userDbStorage.create(friend);
        userDbStorage.addFriend(user.getId(), friend.getId());
        userDbStorage.deleteFriend(user.getId(), friend.getId());
        Assertions.assertEquals(new ArrayList<>(), userDbStorage.getFriends(user.getId()));
    }

    @Test
    void shouldGetFriends() {
        User friend = User.builder()
                .email("friend@mail.ru")
                .login("friend")
                .birthday(LocalDate.of(1991, 4, 27))
                .friends(new HashSet<>())
                .build();
        User friend2 = User.builder()
                .email("friend2@mail.ru")
                .login("friend2")
                .birthday(LocalDate.of(1991, 4, 16))
                .friends(new HashSet<>())
                .build();
        userDbStorage.create(user);
        userDbStorage.create(friend);
        userDbStorage.create(friend2);
        userDbStorage.addFriend(user.getId(), friend.getId());
        userDbStorage.addFriend(user.getId(), friend2.getId());
        Assertions.assertEquals(2, userDbStorage.getFriends(user.getId()).size());
        Assertions.assertEquals(friend, userDbStorage.getFriends(user.getId()).toArray()[0]);
        Assertions.assertEquals(friend2, userDbStorage.getFriends(user.getId()).toArray()[1]);
    }

    @Test
    void shouldGetCommonFriends() {
        User friend = User.builder()
                .email("friend@mail.ru")
                .login("friend")
                .birthday(LocalDate.of(1991, 4, 27))
                .friends(new HashSet<>())
                .build();
        User friend2 = User.builder()
                .email("friend2@mail.ru")
                .login("friend2")
                .birthday(LocalDate.of(1991, 4, 16))
                .friends(new HashSet<>())
                .build();
        userDbStorage.create(user);
        userDbStorage.create(friend);
        userDbStorage.create(friend2);
        userDbStorage.addFriend(friend.getId(), user.getId());
        userDbStorage.addFriend(friend2.getId(), user.getId());
        Assertions.assertEquals(user, userDbStorage.getCommonFriends(friend.getId(), friend2.getId()).toArray()[0]);
    }
}