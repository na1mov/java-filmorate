package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaRatingController {
    private final MpaRatingService mpaRatingService;

    public MpaRatingController(MpaRatingService mpaRatingService) {
        this.mpaRatingService = mpaRatingService;
    }

    @GetMapping("/{id}")
    public MpaRating getMpaRating(@PathVariable("id") Integer ratingMpaId) {
        return mpaRatingService.getMpaRating(ratingMpaId);
    }

    @GetMapping
    public Collection<MpaRating> getMpaRatings() {
        log.info("Получен GET запрос к эндпоинту /mpa на получение всех MPA рейтингов.");
        return mpaRatingService.getMpaRatings();
    }
}
