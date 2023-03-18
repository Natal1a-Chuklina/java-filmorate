package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.dto.film.CreateFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.FilmResponse;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage, GenreStorage genreStorage,
                       MpaStorage mpaStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Collection<FilmResponse> getAll() {
        return filmStorage.getAll().stream()
                .map(this::createFilmResponse)
                .collect(Collectors.toList());
    }

    public FilmResponse createFilm(CreateFilmRequest film) {
        int filmId = filmStorage.add(film);
        return createFilmResponse(film, filmId);
    }

    private FilmResponse createFilmResponse(Film film) {
        FilmResponse filmResponse = new FilmResponse(film.getId(), film.getName(), film.getDescription(),
                film.getReleaseDate(), film.getDuration(), film.getMpa());

        for (Genre genre : film.getGenres()) {
            filmResponse.addGenre(genre);
        }

        return filmResponse;
    }

    private FilmResponse createFilmResponse(CreateFilmRequest film, int filmId) {
        FilmResponse filmResponse = new FilmResponse(filmId, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa());

        filmResponse.setGenres(film.getGenres().stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .collect(Collectors.toList()));

        return filmResponse;
    }

    private FilmResponse createFilmResponse(UpdateFilmRequest film) {
        FilmResponse filmResponse = new FilmResponse(film.getId(), film.getName(), film.getDescription(),
                film.getReleaseDate(), film.getDuration(), film.getMpa());

        filmResponse.setGenres(film.getGenres().stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .collect(Collectors.toList()));

        return filmResponse;
    }

    public FilmResponse updateFilm(UpdateFilmRequest film) {
        if (!filmStorage.isFilmExists(film.getId())) {
            log.warn("Выполнена попытка обновить информацию о фильме с несуществующим id = {}.", film.getId());
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, film.getId()));
        }

        filmStorage.update(film);
        return createFilmResponse(film);
    }

    public FilmResponse getFilmById(int filmId) {
        if (!filmStorage.isFilmExists(filmId)) {
            log.warn("Выполнена попытка получить фильм по несущестующему id = {}", filmId);
            throw new NotFoundException(String.format(Constants.FILM_NOT_FOUND_MESSAGE, filmId));
        }

        return createFilmResponse(filmStorage.getFilm(filmId));
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

    public List<FilmResponse> getBestFilmsList(int count) {
        return filmStorage.getBestFilms(count).stream()
                .map(this::createFilmResponse)
                .collect(Collectors.toList());
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
}
