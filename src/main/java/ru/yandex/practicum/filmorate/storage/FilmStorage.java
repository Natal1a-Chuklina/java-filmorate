package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> getAll();

    boolean isFilmExist(int filmId);

    void add(Film film);

    void update(Film film);

    Film getFilm(int filmId);
}
