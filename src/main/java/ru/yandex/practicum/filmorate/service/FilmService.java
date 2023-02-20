package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private int counter = 1;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film createFilm(Film film) {
        if (filmStorage.isFilmExist(film.getId())) {
            log.warn("Выполнена попытка добавить фильме с уже существующим id = {}.", film.getId());
            throw new AlreadyExistException(String.format(Constants.FILM_ALREADY_EXISTS_MESSAGE, film.getId()));
        } else {
            film.setId(counter++);
            filmStorage.add(film);
            return film;
        }
    }

    public Film updateFilm(Film film) {
        if (!filmStorage.isFilmExist(film.getId())) {
            log.warn("Выполнена попытка обновить информацию о фильме с несуществующим id = {}.", film.getId());
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, film.getId()));
        }

        filmStorage.update(film);
        return film;
    }

    public Film getFilmById(int filmId) {
        if (!filmStorage.isFilmExist(filmId)) {
            log.warn("Выполнена попытка получить фильм по несущестующему id = {}", filmId);
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, filmId));
        }

        return filmStorage.getFilm(filmId);
    }

    public void addLike(int filmId, int userId) {
        if (!filmStorage.isFilmExist(filmId)) {
            log.warn("Выполнена попытка поставить лайк фильму с несуществующим id = {}.", filmId);
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, filmId));
        } else if (!userStorage.isUserExist(userId)) {
            log.warn("Выполнена попытка поставить лайк фильму пользователем с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        if (filmStorage.getFilm(filmId).getLikes().contains(userId)) {
            log.warn("Выполнена попытка повторно поставить лайк фильму с id = {} пользователем с id = {}",
                    filmId, userId);
            throw new AlreadyExistException(String.format(Constants.USER_ALREADY_LIKED_FILM_MESSAGE, userId, filmId));
        }

        filmStorage.getFilm(filmId).addLike(userId);
        log.info("Пользователь с id = {} поставил лайк фильму с id = {}", userId, filmId);
    }

    public void deleteLike(int filmId, int userId) {
        if (!filmStorage.isFilmExist(filmId)) {
            log.warn("Выполнена попытка удалить лайк фильма с несуществующим id = {}.", filmId);
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, filmId));
        } else if (!userStorage.isUserExist(userId)) {
            log.warn("Выполнена попытка удалить лайк фильму пользователем с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        if (!filmStorage.getFilm(filmId).getLikes().contains(userId)) {
            log.warn("Выполнена попытка удалить несущестующий лайк у фильма с id = {} пользователем с id = {}",
                    filmId, userId);
            throw new AlreadyExistException(String.format(Constants.USER_NOT_LIKED_FILM_MESSAGE, userId, filmId));
        }

        filmStorage.getFilm(filmId).deleteLike(userId);
        log.info("Пользователь с id = {} удалил лайк у фильма с id = {}", userId, filmId);
    }

    public List<Film> getBestFilmsList(int count) {
        log.info("Получен топ {} фильмов", count);
        return filmStorage.getAll().stream().sorted(Comparator.comparingInt(film -> ((Film) film).getLikes().size())
                .reversed()).limit(count).collect(Collectors.toList());
    }
}
