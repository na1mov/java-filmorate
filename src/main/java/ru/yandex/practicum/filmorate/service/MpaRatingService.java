package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaRatingService {
    private final MpaRatingDbStorage mpaRatingDbStorage;

    public MpaRating getMpaRating(Integer ratingMpaId) {
        return mpaRatingDbStorage.getMpaRating(ratingMpaId);
    }

    public Collection<MpaRating> getMpaRatings() {
        return mpaRatingDbStorage.getMpaRatings();
    }
}
