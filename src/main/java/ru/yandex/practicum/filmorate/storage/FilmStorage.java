package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Collection<Film> getAll();

    boolean isFilmExists(int filmId);

    boolean isFilmContainsUserLike(int filmId, int userId);

    int add(Film film);

    void update(Film film);

    void delete(int filmId);

    Film getFilm(int filmId);

    void addLike(int filmId, int userId);

    void deleteLike(int filmId, int userId);

    Collection<Film> getBestFilms(int count);

    List<Film> getSortedFilmsByDirId(long directorId, String sort);

    Collection<Film> getCommonFilms(int userId, int friendId);

    List<Film> getSortedFilmByQuery(String query, String by);

    List<Film> getFilteredBestFilms(int count, Integer genreId, Integer year);
}
