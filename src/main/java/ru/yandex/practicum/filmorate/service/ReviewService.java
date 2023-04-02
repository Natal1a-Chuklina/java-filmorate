package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ReviewService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final ReviewStorage reviewStorage;

    public Review createReview(Review review) {
        Integer userId = review.getUserId();
        Integer filmId = review.getFilmId();
        throwExceptionIfFilmDoesNotExist(
                "Выполнена попытка создать отзыв для фильма с несуществующим id = {}.",
                filmId);
        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка создать отзыв для фильма пользователем с несуществующим id = {}.",
                userId);
        boolean hasUserAlreadyLeftReviewForFilm = getReviewsByFilmId(filmId, getAllReviews().size())
                .stream()
                .map(Review::getUserId)
                .anyMatch(x -> x.equals(userId));
        if (hasUserAlreadyLeftReviewForFilm) {
            log.warn("Пользователь с id {} уже оставлял отзыв для фильма с id {}", userId, filmId);
            throw new AlreadyExistException(String.format(Constants.USER_ALREADY_LEFT_REVIEW_FOR_FILM_MESSAGE,
                    userId, filmId));
        }
        review = reviewStorage.create(review);
        log.info("Добавлен отзыв: {}", review);
        return review;
    }

    public Review updateReview(Review review) {
        throwExceptionIfReviewDoesNotExist(review.getReviewId());
        review = reviewStorage.update(review);
        log.info("Обновлен отзыв: {}", review);
        return review;
    }

    public void removeReview(Integer id) {
        throwExceptionIfReviewDoesNotExist(id);
        int rows = reviewStorage.remove(id);
        if (rows > 0) {
            log.info("Отзыв с id {} был удален", id);
        }
    }

    public Review findReviewById(Integer id) {
        throwExceptionIfReviewDoesNotExist(id);
        Review review = reviewStorage.findReviewById(id);
        log.info("В БД найден отзыв: {}", review);
        return review;
    }

    public List<Review> getAllReviews() {
        List<Review> reviews = reviewStorage.findAll();
        log.info("Количество отзывов: {}", reviews.size());
        return reviews;
    }

    public List<Review> getReviewsByFilmId(Integer filmId, Integer count) {
        throwExceptionIfFilmDoesNotExist(
                "Выполнена попытка получить отзыв для фильма с несуществующим id = {}.", filmId);
        List<Review> reviews = reviewStorage.findReviewsByFilmId(filmId, count);
        log.info("Для фильма с id {} количество отзывов: {}", filmId, reviews.size());
        return reviews;
    }

    public void addLikeToReview(Integer id, Integer userId) {
        throwExceptionIfReviewDoesNotExist(id);
        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка поставить лайк отзыву пользователем с несуществующим id = {}.", userId);

        if (reviewStorage.hasUserAlreadyLeftLikeForFilm(id, userId)) {
            log.warn("Пользователь с id {} уже оставлял лайк отзыву с id {}", userId, id);
            throw new AlreadyExistException(String.format(Constants.USER_ALREADY_LEFT_LIKE_FOR_REVIEW_MESSAGE,
                    userId, id));
        }
        if (reviewStorage.hasUserAlreadyLeftDislikeForFilm(id, userId)) {
            log.info("Пользователь передумал");
            removeDislikeOfReview(id, userId);
        }
        reviewStorage.addLike(id, userId);
        log.info("Пользователь с id {} добавил лайк отзыву с id {}", userId, id);
    }

    public void addDislikeToReview(Integer id, Integer userId) {
        throwExceptionIfReviewDoesNotExist(id);
        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка поставить дизлайк отзыву пользователем с несуществующим id = {}.", userId);

        if (reviewStorage.hasUserAlreadyLeftDislikeForFilm(id, userId)) {
            log.warn("Пользователь с id {} уже оставлял дизлайк отзыву с id {}", userId, id);
            throw new AlreadyExistException(String.format(Constants.USER_ALREADY_LEFT_DISLIKE_FOR_REVIEW_MESSAGE,
                    userId, id));
        }
        if (reviewStorage.hasUserAlreadyLeftLikeForFilm(id, userId)) {
            log.info("Пользователь передумал");
            removeLikeOfReview(id, userId);
        }
        reviewStorage.addDislike(id, userId);
        log.info("Пользователь с id {} добавил дизлайк отзыву с id {}", userId, id);
    }

    public void removeLikeOfReview(Integer id, Integer userId) {
        throwExceptionIfReviewDoesNotExist(id);
        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка удалить лайк отзыву пользователем с несуществующим id = {}.", userId);

        if (!reviewStorage.hasUserAlreadyLeftLikeForFilm(id, userId)) {
            log.warn("Выполнена попытка удалить лайк отзыву пользователем, который его не оставлял.");
            throw new NotFoundException(String.format(Constants.USER_DID_NOT_LEAVE_LIKE_FOR_REVIEW_MESSAGE, userId, id));
        }
        reviewStorage.removeLike(id, userId);
        log.info("Пользователь с id {} удалил лайк у отзыва с id {}", userId, id);
    }

    public void removeDislikeOfReview(Integer id, Integer userId) {
        throwExceptionIfReviewDoesNotExist(id);
        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка удалить дизлайк отзыву пользователем с несуществующим id = {}.", userId);

        if (!reviewStorage.hasUserAlreadyLeftDislikeForFilm(id, userId)) {
            log.warn("Выполнена попытка удалить дизлайк отзыву пользователем, который его не оставлял.");
            throw new NotFoundException(String.format(Constants.USER_DID_NOT_LEAVE_DISLIKE_FOR_REVIEW_MESSAGE, userId, id));
        }
        reviewStorage.removeDislike(id, userId);
        log.info("Пользователь с id {} удалил дизлайк у отзыва с id {}", userId, id);
    }

    private void throwExceptionIfFilmDoesNotExist(String logMessage, int filmId) {
        if (!filmStorage.isFilmExists(filmId)) {
            log.warn(logMessage, filmId);
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, filmId));
        }
    }

    private void throwExceptionIfUserDoesNotExist(String logMessage, int userId) {
        if (!userStorage.isUserExistsById(userId)) {
            log.warn(logMessage, userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }
    }

    private void throwExceptionIfReviewDoesNotExist(int reviewId) {
        if (!reviewStorage.isReviewExists(reviewId)) {
            log.warn("Выполнена попытка получить отзыв по несущестующему id = {}", reviewId);
            throw new NotFoundException(String.format(Constants.REVIEW_NOT_FOUND_MESSAGE, reviewId));
        }
    }
}
