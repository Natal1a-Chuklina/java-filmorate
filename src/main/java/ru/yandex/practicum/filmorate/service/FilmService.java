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
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
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
    private final DirectorStorage directorStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage, GenreStorage genreStorage,
                       MpaStorage mpaStorage, DirectorStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.directorStorage = directorStorage;
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

    public List<Film> getSortedFilmByQuery(String query, String by) {
        if (!by.equals("director") &&
                !by.equals("title") &&
                !by.equals("director,title") &&
                !by.equals("title,director")) {
            throw new NotFoundException("Существует сортировка только по title или director или обоим сразу");
        }
        return filmStorage.getSortedFilmByQuery(query, by);
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

    private void checkDirectors(Film film) {
        for (Director director : film.getDirectors()) {
            if (!directorStorage.isDirectorExists(director.getId())) {
                log.warn("Выполнена попытка получить режиссера по несуществующему id = {}", director.getId());
                throw new NotFoundException(String.format(Constants.DIRECTOR_NOT_FOUND, director.getId()));
            }
        }
    }
}
