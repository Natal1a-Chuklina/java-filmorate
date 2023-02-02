package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger logger = LoggerFactory.getLogger(FilmController.class);
    private final Map<Integer, Film> films = new HashMap<>();
    private int counter = 1;

    @GetMapping
    public Collection<Film> getAll() {
        logger.info("Получен список фильмов длиной {}.", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        if (films.containsKey(film.getId())) {
            logger.info("Выполнена попытка добавить фильме с уже существующим id = {}.", film.getId());
            throw new AlreadyExistException(String.format("Фильм с id = %d уже существует", film.getId()));
        } else {
            logger.info("Добавлен фильм с id = {}.", counter);
            film.setId(counter++);
            films.put(film.getId(), film);
            return film;
        }
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            logger.info("Выполнена попытка обновить информацию о фильме с несуществующим id = {}.", film.getId());
            throw new NotFoundException(String.format("Фильм с идентификатором %d не найден", film.getId()));
        }

        BeanUtils.copyProperties(film, films.get(film.getId()), "id");
        logger.info("Информация о фильме с id = {} обновлена.", film.getId());
        return film;
    }
}
