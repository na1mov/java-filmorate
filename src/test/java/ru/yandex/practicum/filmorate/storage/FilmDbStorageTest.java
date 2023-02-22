package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;
    private Film film;

    @BeforeEach
    void beforeEach() {
        jdbcTemplate.update("DELETE FROM film");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM film_like");
        film = Film.builder()
                .name("FilmName")
                .description("Description")
                .releaseDate(LocalDate.of(1999, 9, 9))
                .duration(99)
                .likes(new HashSet<>())
                .mpa(MpaRating.builder().id(3).name("PG-13").build())
                .genres(new HashSet<>())
                .build();
    }

    @Test
    void shouldAddFilmWithRightIdAndGetItBack() {
        filmDbStorage.create(film);
        Assertions.assertEquals(film, filmDbStorage.getFilm(film.getId()));
    }

    @Test
    void shouldUpdateFilmNameAndDuration() {
        filmDbStorage.create(film);
        film.setName("NewFilmName");
        film.setDuration(77);
        filmDbStorage.update(film);
        Assertions.assertEquals("NewFilmName", filmDbStorage.getFilm(film.getId()).getName());
        Assertions.assertEquals(77, filmDbStorage.getFilm(film.getId()).getDuration());
    }

    @Test
    void shouldDeleteFilmAndGetEmptyFilmsList() {
        filmDbStorage.create(film);
        filmDbStorage.delete(film.getId());
        Assertions.assertEquals(new ArrayList<>(), filmDbStorage.getFilms());
    }

    @Test
    void shouldGetFilmFromDb() {
        filmDbStorage.create(film);
        Assertions.assertEquals(film, filmDbStorage.getFilm(film.getId()));
    }

    @Test
    void shouldGetFilmsWithRightListSizeAndContent() {
        filmDbStorage.create(film);
        Assertions.assertEquals(1, filmDbStorage.getFilms().size());
        Assertions.assertEquals(film, filmDbStorage.getFilms().toArray()[0]);
    }

    @Test
    void shouldAddLikeAndGetFilmWithLikesListWithRightConditions() {
        User user = User.builder()
                .email("user@mail.ru")
                .login("user")
                .birthday(LocalDate.of(1991, 6, 28))
                .friends(new HashSet<>())
                .build();
        user = userDbStorage.create(user);
        filmDbStorage.create(film);
        filmDbStorage.addLike(film.getId(), user.getId());
        Set<Integer> filmLikes = filmDbStorage.getFilm(film.getId()).getLikes();
        Assertions.assertEquals(1, filmLikes.size());
        Assertions.assertEquals(user.getId(), filmLikes.toArray()[0]);
    }

    @Test
    void shouldRemoveLikeAndGetEmptyLikeList() {
        User user = User.builder()
                .email("user@mail.ru")
                .login("user")
                .birthday(LocalDate.of(1991, 6, 28))
                .friends(new HashSet<>())
                .build();
        userDbStorage.create(user);
        filmDbStorage.create(film);
        filmDbStorage.addLike(film.getId(), user.getId());
        filmDbStorage.removeLike(film.getId(), user.getId());
        Assertions.assertEquals(new HashSet<>(), filmDbStorage.getFilm(film.getId()).getLikes());
    }

    @Test
    void shouldGetPopularFilms() {
        User user = User.builder()
                .email("user@mail.ru")
                .login("user")
                .birthday(LocalDate.of(1991, 6, 28))
                .friends(new HashSet<>())
                .build();
        User otherUser = User.builder()
                .email("friend@mail.ru")
                .login("friend")
                .birthday(LocalDate.of(1991, 4, 27))
                .friends(new HashSet<>())
                .build();
        Film otherFilm = Film.builder()
                .name("FilmName")
                .description("Description")
                .releaseDate(LocalDate.of(1999, 9, 9))
                .duration(99)
                .likes(new HashSet<>())
                .mpa(MpaRating.builder().id(3).name("PG-13").build())
                .genres(new HashSet<>())
                .build();
        userDbStorage.create(user);
        userDbStorage.create(otherUser);
        filmDbStorage.create(film);
        filmDbStorage.create(otherFilm);
        filmDbStorage.addLike(film.getId(), user.getId());
        filmDbStorage.addLike(film.getId(), otherUser.getId());
        filmDbStorage.addLike(otherFilm.getId(), user.getId());
        List<Film> popularFilms = filmDbStorage.getPopularFilms(2);
        Assertions.assertEquals(2, popularFilms.size());
        Assertions.assertEquals(filmDbStorage.getFilm(film.getId()), popularFilms.toArray()[0]);
    }
}