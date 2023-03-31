package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
@Validated
public class FilmController {
    private static final String DEFAULT_BEST_FILMS_COUNT = "10";
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getAll() {
        log.info("Попытка получить список фильмов");
        return filmService.getAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Попытка создать фильм: {}", film);
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Попытка обновить информацию о фильме: {}", film);
        return filmService.updateFilm(film);
    }

    @GetMapping("/{filmId}")
    public Film getFilmById(@PathVariable int filmId) {
        log.info("Попытка получить информацию о фильме с id = {}", filmId);
        return filmService.getFilmById(filmId);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable int filmId) {
        log.info("Получен запрос на удаление фильма с id = {}", filmId);
        filmService.deleteFilm(filmId);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable int filmId, @PathVariable int userId) {
        log.info("Попытка добавить лайк фильму с id = {} пользователем с id = {}", filmId, userId);
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void deleteLike(@PathVariable int filmId, @PathVariable int userId) {
        log.info("Попытка удалить лайк у фильма с id = {} пользователем с id = {}", filmId, userId);
        filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getBestFilmsList(@RequestParam(defaultValue = DEFAULT_BEST_FILMS_COUNT)
                                       @Positive int count) {
        log.info("Попытка получить топ {} фильмов", count);
        return filmService.getBestFilmsList(count);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getSortedFilmsByDirId(@PathVariable long directorId,
                                            @RequestParam(value = "sortBy") String sort) {
        log.info("Попытка получить список фильмов режиссера с id = {}, с сортировкой по {}", directorId, sort);
        return filmService.getSortedFilmsByDirId(directorId, sort);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        log.info("Попытка получить список общих любимых фильмов пользователей с id: {} и {}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/search")
    public List<Film> getSortedFilmByQuery(@RequestParam(value = "query", required = false) String query,
                                            @RequestParam(value = "by", required = false) String by) {
        if (query == null && by == null) {
            log.info("Попытка получить топ 10 фильмов по лайкам");
            return filmService.getBestFilmsList(10);
        }
        log.info("Попытка получить фильмы, отсортированных по {}, имеющих подстроку {}", by, query);
        return filmService.getSortedFilmByQuery(query, by);
    }
}
