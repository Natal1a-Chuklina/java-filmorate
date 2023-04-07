package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class RecommendationService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public RecommendationService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Collection<Film> getRecommendations(int userId) {
        if (!userStorage.isUserExistsById(userId)) {
            log.warn("Попытка получить рекоммендации для пользоватея с несуществующим id = {}", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        Collection<Film> films = filmStorage.getRecommendations(userId);
        log.info("Пользователь с id = {} получил список рекомендуемых фильмов длиной {}", userId, films.size());

        return films;
    }
}
