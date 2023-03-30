package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final FilmService filmService;

    @PostMapping
    public Review createReview(@Valid @RequestBody Review review) {
        return filmService.createReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return filmService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable Integer id) {
        filmService.removeReview(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Integer id) {
        return filmService.findReviewById(id);
    }

    @GetMapping
    public List<Review> getReviewsByFilmIdOrGetAll(
            @RequestParam(value = "filmId", required = false) Integer filmId,
            @RequestParam(value = "count", defaultValue = "10", required = false) @Positive Integer count) {
        if (filmId == null) {
            return filmService.getAllReviews();
        }
        return filmService.getReviewsByFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeToReview(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.addLikeToReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeToReview(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.addDislikeToReview(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLikeOfReview(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.removeLikeOfReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislikeOfReview(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.removeDislikeOfReview(id, userId);
    }
}
