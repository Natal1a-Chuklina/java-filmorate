package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final ReviewStorage reviewStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage, GenreStorage genreStorage,
                       MpaStorage mpaStorage, ReviewStorage reviewStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.reviewStorage = reviewStorage;
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film createFilm(Film film) {
        return filmStorage.getFilm(filmStorage.add(film));
    }

    public Film updateFilm(Film film) {
        if (!filmStorage.isFilmExists(film.getId())) {
            log.warn("Выполнена попытка обновить информацию о фильме с несуществующим id = {}.", film.getId());
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, film.getId()));
        }

        filmStorage.update(film);
        return filmStorage.getFilm(film.getId());
    }

    public Film getFilmById(int filmId) {
        if (!filmStorage.isFilmExists(filmId)) {
            log.warn("Выполнена попытка получить фильм по несущестующему id = {}", filmId);
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, filmId));
        }

        return filmStorage.getFilm(filmId);
    }

    public void addLike(int filmId, int userId) {
        if (!filmStorage.isFilmExists(filmId)) {
            log.warn("Выполнена попытка поставить лайк фильму с несуществующим id = {}.", filmId);
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, filmId));
        } else if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка поставить лайк фильму пользователем с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        if (filmStorage.isFilmContainsUserLike(filmId, userId)) {
            log.warn("Выполнена попытка повторно поставить лайк фильму с id = {} пользователем с id = {}",
                    filmId, userId);
            throw new AlreadyExistException(String.format(Constants.USER_ALREADY_LIKED_FILM_MESSAGE, userId, filmId));
        } else {
            filmStorage.addLike(filmId, userId);
        }
    }

    public void deleteLike(int filmId, int userId) {
        if (!filmStorage.isFilmExists(filmId)) {
            log.warn("Выполнена попытка удалить лайк фильма с несуществующим id = {}.", filmId);
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, filmId));
        } else if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка удалить лайк фильму пользователем с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        if (filmStorage.isFilmContainsUserLike(filmId, userId)) {
            filmStorage.deleteLike(filmId, userId);
        } else {
            log.warn("Выполнена попытка удалить несущестующий лайк у фильма с id = {} пользователем с id = {}",
                    filmId, userId);
            throw new AlreadyExistException(String.format(Constants.USER_NOT_LIKED_FILM_MESSAGE, userId, filmId));
        }
    }

    public List<Film> getBestFilmsList(int count) {
        return new ArrayList<>(filmStorage.getBestFilms(count));
    }

    public List<Genre> getAllGenres() {
        return new ArrayList<>(genreStorage.getAll());
    }

    public Genre getGenreById(int genreId) {
        if (!genreStorage.isGenreExists(genreId)) {
            log.warn("Выполнена попытка получить жанр по несущестующему id = {}", genreId);
            throw new NotFoundException(String.format(Constants.GENRE_NOT_FOUND_MESSAGE, genreId));
        }

        return genreStorage.getById(genreId);
    }

    public List<Mpa> getAllRatings() {
        return new ArrayList<>(mpaStorage.getAll());
    }

    public Mpa getRatingById(int mpaId) {
        if (!mpaStorage.isRatingExists(mpaId)) {
            log.warn("Выполнена попытка получить рейтинг по несущестующему id = {}", mpaId);
            throw new NotFoundException(String.format(Constants.RATING_NOT_FOUND_MESSAGE, mpaId));
        }

        return mpaStorage.getById(mpaId);
    }

    public Review createReview(Review review) {
        if (!filmStorage.isFilmExists(review.getFilmId())) {
            log.warn("Выполнена попытка создать отзыв для фильма с несуществующим id = {}.", review.getFilmId());
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, review.getFilmId()));
        } else if (!userStorage.isUserExistsById(review.getUserId())) {
            log.warn("Выполнена попытка создать отзыв для фильма пользователем с несуществующим id = {}.",
                    review.getUserId());
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, review.getUserId()));
        }
        review = reviewStorage.create(review);
        log.info("Review is added: {}", review);
        return review;
    }

    public Review updateReview(Review review) {
        if (reviewStorage.isReviewExists(review.getReviewId())) {
            log.warn("Выполнена попытка получить отзыв по несущестующему id = {}", review.getReviewId());
            throw new NotFoundException(String.format(Constants.REVIEW_NOT_FOUND_MESSAGE, review.getReviewId()));
        }
        review = reviewStorage.update(review);
        log.info("Review is updated: {}", review);
        return review;
    }

    public void removeReview(Integer id) {
        if (reviewStorage.isReviewExists(id)) {
            log.warn("Выполнена попытка получить отзыв по несущестующему id = {}", id);
            throw new NotFoundException(String.format(Constants.REVIEW_NOT_FOUND_MESSAGE, id));
        }
        int rows = reviewStorage.remove(id);
        if (rows > 0) {
            log.info("Review with id {} was removed", id);
        }
    }

    public Review findReviewById(Integer id) {
        Review review = reviewStorage.findReviewById(id);
        log.info("Review is found in DB: {}", review);
        return review;
    }

    public List<Review> getAllReviews() {
        List<Review> reviews = reviewStorage.findAll();
        log.info("Reviews quantity is: {}", reviews.size());
        return reviews;
    }

    public List<Review> getReviewsByFilmId(Integer filmId, Integer count) {
        if (!filmStorage.isFilmExists(filmId)) {
            log.warn("Выполнена попытка получить отзыв для фильма с несуществующим id = {}.", filmId);
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, filmId));
        }
        List<Review> reviews = reviewStorage.findReviewsByFilmId(filmId, count);
        if (reviews != null) {
            log.info("Reviews quantity for film with id {} is: {}", filmId, reviews.size());
        }

        return reviews;
    }

    public void addLikeToReview(Integer id, Integer userId) {
        if (reviewStorage.isReviewExists(id)) {
            log.warn("Выполнена попытка получить отзыв по несущестующему id = {}", id);
            throw new NotFoundException(String.format(Constants.REVIEW_NOT_FOUND_MESSAGE, id));
        } else if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка поставить лайк отзыву пользователем с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        reviewStorage.addLike(id, userId);
        log.info("User with id {} added a like to review with id {}", userId, id);
    }

    public void addDislikeToReview(Integer id, Integer userId) {
        if (reviewStorage.isReviewExists(id)) {
            log.warn("Выполнена попытка получить отзыв по несущестующему id = {}", id);
            throw new NotFoundException(String.format(Constants.REVIEW_NOT_FOUND_MESSAGE, id));
        } else if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка поставить лайк отзыву пользователем с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        reviewStorage.addDislike(id, userId);
        log.info("User with id {} added a dislike to review with id {}", userId, id);
    }

    public void removeLikeOfReview(Integer id, Integer userId) {
        if (reviewStorage.isReviewExists(id)) {
            log.warn("Выполнена попытка получить отзыв по несущестующему id = {}", id);
            throw new NotFoundException(String.format(Constants.REVIEW_NOT_FOUND_MESSAGE, id));
        } else if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка удалить лайк отзыву пользователем с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        reviewStorage.removeLike(id, userId);
        log.info("User with id {} removed a like of review with id {}", userId, id);
    }

    public void removeDislikeOfReview(Integer id, Integer userId) {
        if (reviewStorage.isReviewExists(id)) {
            log.warn("Выполнена попытка получить отзыв по несущестующему id = {}", id);
            throw new NotFoundException(String.format(Constants.REVIEW_NOT_FOUND_MESSAGE, id));
        } else if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка удалить лайк отзыву пользователем с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        reviewStorage.removeDislike(id, userId);
        log.info("User with id {} removed a dislike of review with id {}", userId, id);
    }
}
