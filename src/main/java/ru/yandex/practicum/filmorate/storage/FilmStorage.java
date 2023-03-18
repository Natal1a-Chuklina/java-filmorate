package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.film.CreateFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> getAll();

    boolean isFilmExists(int filmId);

    boolean isFilmContainsUserLike(int filmId, int userId);

    int add(CreateFilmRequest film);

    void update(UpdateFilmRequest film);

    Film getFilm(int filmId);

    void addLike(int filmId, int userId);

    void deleteLike(int filmId, int userId);

    Collection<Film> getBestFilms(int count);
}
