package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    int remove(Integer id);

    Review findReviewById(Integer id);

    List<Review> findAll();

    List<Review> findReviewsByFilmId(Integer filmId, Integer count);

    void addLike(Integer id, Integer userId);

    void addDislike(Integer id, Integer userId);

    void removeLike(Integer id, Integer userId);

    void removeDislike(Integer id, Integer userId);

    boolean isReviewExists(Integer id);

    boolean hasUserAlreadyLeftLikeForReview(Integer reviewId, Integer userId);

    boolean hasUserAlreadyLeftDislikeForReview(Integer reviewId, Integer userId);

    boolean hasUserAlreadyLeftReviewForFilm(Integer filmId, Integer userId);
}
