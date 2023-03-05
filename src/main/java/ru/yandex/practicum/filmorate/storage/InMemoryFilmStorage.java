package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films;

    public InMemoryFilmStorage() {
        films = new HashMap<>();
    }

    @Override
    public Collection<Film> getAll() {
        log.info("Получен список фильмов длиной {}.", films.size());
        return films.values();
    }

    @Override
    public boolean isFilmExist(int filmId) {
        return films.containsKey(filmId);
    }

    @Override
    public void add(Film film) {
        films.put(film.getId(), film);
        log.info("Добавлен фильм с id = {}.", film.getId());
    }

    @Override
    public void update(Film film) {
        BeanUtils.copyProperties(film, films.get(film.getId()), "id");
        log.info("Информация о фильме с id = {} обновлена.", film.getId());
    }

    @Override
    public Film getFilm(int filmId) {
        return films.get(filmId);
    }


}
