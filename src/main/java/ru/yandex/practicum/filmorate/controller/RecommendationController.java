package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.RecommendationService;

import java.util.Collection;

@RestController
@Slf4j
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/users/{userId}/recommendations")
    public Collection<Film> getRecommendations(@PathVariable int userId) {
        log.info("Попытка получить рекоммендации для пользователя с id = {}", userId);
        return recommendationService.getRecommendations(userId);
    }
}
