package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
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
    private final DirectorStorage directorStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage, GenreStorage genreStorage,
                       MpaStorage mpaStorage, ReviewStorage reviewStorage, DirectorStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.directorStorage = directorStorage;
        this.reviewStorage = reviewStorage;
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film createFilm(Film film) {
        checkDirectors(film);
        return filmStorage.getFilm(filmStorage.add(film));
    }

    public Film updateFilm(Film film) {
        checkDirectors(film);
        throwExceptionIfFilmDoesNotExist(
                "Выполнена попытка обновить информацию о фильме с несуществующим id = {}.",
                film.getId());

        filmStorage.update(film);
        return filmStorage.getFilm(film.getId());
    }

    public Film getFilmById(int filmId) {
        throwExceptionIfFilmDoesNotExist(
                "Выполнена попытка получить фильм по несуществующему id = {}",
                filmId);

        return filmStorage.getFilm(filmId);
    }

    public void deleteFilm(int filmId) {
        if (!filmStorage.isFilmExists(filmId)) {
            log.warn("Выполнена попытка удалить фильм по несуществующему id = {}", filmId);
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, filmId));
        }

        filmStorage.delete(filmId);
    }

    public void addLike(int filmId, int userId) {
        throwExceptionIfFilmDoesNotExist(
                "Выполнена попытка поставить лайк фильму с несуществующим id = {}.",
                filmId);

        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка поставить лайк фильму пользователем с несуществующим id = {}.",
                userId);

        if (filmStorage.isFilmContainsUserLike(filmId, userId)) {
            log.warn("Выполнена попытка повторно поставить лайк фильму с id = {} пользователем с id = {}",
                    filmId, userId);
            throw new AlreadyExistException(String.format(Constants.USER_ALREADY_LIKED_FILM_MESSAGE, userId, filmId));
        } else {
            filmStorage.addLike(filmId, userId);
        }
    }

    public void deleteLike(int filmId, int userId) {
        throwExceptionIfFilmDoesNotExist(
                "Выполнена попытка удалить лайк фильма с несуществующим id = {}.",
                filmId);

        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка удалить лайк фильму пользователем с несуществующим id = {}.",
                userId);

        if (filmStorage.isFilmContainsUserLike(filmId, userId)) {
            filmStorage.deleteLike(filmId, userId);
        } else {
            log.warn("Выполнена попытка удалить несуществующий лайк у фильма с id = {} пользователем с id = {}",
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

    public List<Film> getCommonFilms(int userId, int friendId) {
        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка получить список общих любимых фильмов c пользователем с несуществующим id = {}.",
                userId);

        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка получить список общих любимых фильмов c пользователем с несуществующим id = {}.",
                friendId);

        return new ArrayList<>(filmStorage.getCommonFilms(userId, friendId));
    }

    public Genre getGenreById(int genreId) {
        if (!genreStorage.isGenreExists(genreId)) {
            log.warn("Выполнена попытка получить жанр по несуществующему id = {}", genreId);
            throw new NotFoundException(String.format(Constants.GENRE_NOT_FOUND_MESSAGE, genreId));
        }

        return genreStorage.getById(genreId);
    }

    public List<Mpa> getAllRatings() {
        return new ArrayList<>(mpaStorage.getAll());
    }

    public Mpa getRatingById(int mpaId) {
        if (!mpaStorage.isRatingExists(mpaId)) {
            log.warn("Выполнена попытка получить рейтинг по несуществующему id = {}", mpaId);
            throw new NotFoundException(String.format(Constants.RATING_NOT_FOUND_MESSAGE, mpaId));
        }

        return mpaStorage.getById(mpaId);
    }

    public List<Film> getSortedFilmsByDirId(long directorId, String sort) {
        if (!sort.equals("year") && !sort.equals("likes")) {
            throw new NotFoundException("Существует сортировка только по year или likes");
        }
        if (!directorStorage.isDirectorExists(directorId)) {
            log.warn("Выполнена попытка получить режиссера по несуществующему id = {}", directorId);
            throw new NotFoundException(String.format(Constants.DIRECTOR_NOT_FOUND, directorId));
        }
        return filmStorage.getSortedFilmsByDirId(directorId, sort);
    }

    public Review createReview(Review review) {
        throwExceptionIfFilmDoesNotExist(
                "Выполнена попытка создать отзыв для фильма с несуществующим id = {}.",
                review.getFilmId());
        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка создать отзыв для фильма пользователем с несуществующим id = {}.",
                review.getUserId());
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
        if (reviews != null) {
            log.info("Для фильма с id {} количество отзывов: {}", filmId, reviews.size());
        }

        return reviews;
    }

    public void addLikeToReview(Integer id, Integer userId) {
        throwExceptionIfReviewDoesNotExist(id);
        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка поставить лайк отзыву пользователем с несуществующим id = {}.", userId);

        reviewStorage.addLike(id, userId);
        log.info("Пользователь с id {} добавил лайк отзыву с id {}", userId, id);
    }

    public void addDislikeToReview(Integer id, Integer userId) {
        throwExceptionIfReviewDoesNotExist(id);
        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка поставить дизлайк отзыву пользователем с несуществующим id = {}.", userId);

        reviewStorage.addDislike(id, userId);
        log.info("Пользователь с id {} добавил дизлайк отзыву с id {}", userId, id);
    }

    public void removeLikeOfReview(Integer id, Integer userId) {
        throwExceptionIfReviewDoesNotExist(id);
        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка удалить лайк отзыву пользователем с несуществующим id = {}.", userId);

        reviewStorage.removeLike(id, userId);
        log.info("Пользователь с id {} удалил лайк у отзыва с id {}", userId, id);
    }

    public void removeDislikeOfReview(Integer id, Integer userId) {
        throwExceptionIfReviewDoesNotExist(id);
        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка удалить дизлайк отзыву пользователем с несуществующим id = {}.", userId);

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
        if (reviewStorage.isReviewExists(reviewId)) {
            log.warn("Выполнена попытка получить отзыв по несущестующему id = {}", reviewId);
            throw new NotFoundException(String.format(Constants.REVIEW_NOT_FOUND_MESSAGE, reviewId));
        }
    }

    private void checkDirectors(Film film) {
        for (Director director : film.getDirectors()) {
            if (!directorStorage.isDirectorExists(director.getId())) {
                log.warn("Выполнена попытка получить режиссера по несуществующему id = {}", director.getId());
                throw new NotFoundException(String.format(Constants.DIRECTOR_NOT_FOUND, director.getId()));
            }
        }
    }
}
