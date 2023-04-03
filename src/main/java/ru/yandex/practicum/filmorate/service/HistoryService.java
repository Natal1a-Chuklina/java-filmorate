package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventTypeStatus;
import ru.yandex.practicum.filmorate.model.EventUser;
import ru.yandex.practicum.filmorate.model.OperationStatus;
import ru.yandex.practicum.filmorate.storage.HistoryEventDbUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class HistoryService {
    private final UserStorage userStorage;
    private final HistoryEventDbUserStorage history;

    public HistoryService(UserStorage userStorage, HistoryEventDbUserStorage history) {
        this.userStorage = userStorage;
        this.history = history;
    }

    public List<EventUser> getHistoryUser(int userId) {
        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка найти пользователя с несуществующим id = {}.",
                userId);

        log.info("Получена история действий пользователя с id = {}.", userId);
        return history.findHistoryUserById(userId);
    }

    public void createHistoryUser(int userId, OperationStatus operation, EventTypeStatus eventType, int entityId) {
        EventUser eventUser = EventUser.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .build();

        if (eventUser != null) {
            history.save(eventUser);
        }

        log.info("История действий пользователя с id = {} сохранена.", userId);

    }

    private void throwExceptionIfUserDoesNotExist(String logMessage, int userId) {
        if (!userStorage.isUserExistsById(userId)) {
            log.warn(logMessage, userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }
    }
}
