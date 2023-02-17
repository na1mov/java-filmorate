package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class MpaRatingDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaRating getMpaRating(Integer ratingMpaId) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM rating_mpa WHERE rating_mpa_id = ?",
                    this::mapRowToMpaRating, ratingMpaId);
        } catch (Exception e) {
            throw new NotFoundException(String.format("Рейтинга MPA с ID:%d нет в базе.", ratingMpaId));
        }
    }

    public Collection<MpaRating> getMpaRatings() {
        String sqlQuery = "SELECT * FROM rating_mpa";
        return jdbcTemplate.query(sqlQuery, this::mapRowToMpaRating);
    }

    private MpaRating mapRowToMpaRating(ResultSet resultSet, int rowNum) throws SQLException {
        return MpaRating.builder()
                .id(resultSet.getInt("rating_mpa_id"))
                .name(resultSet.getString("name"))
                .build();
    }
}
