package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

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

        Collection<Film> userLikes = filmStorage.getLikesByUserId(userId);

        return userStorage.getSimilarInterestUsers(userId).stream() // получение спикска пользователей со схожими интересами
                .map(user -> filmStorage.getLikesByUserId(user.getId())) // получение списков фильмов с лайками от каждого ползователя
                .flatMap(Collection::stream) // объединение всех фильмов в общий список
                .filter(film -> !userLikes.contains(film)) // удаление фильмов с лайками от субъекта
                .collect(groupingBy(identity(), counting())) // формирование мапы с фильмом и кол-вом его лайков от пользователе из выборки
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())) //сортировка по кол-ву лайков
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}