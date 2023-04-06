package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventTypeStatus;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.OperationStatus;
import ru.yandex.practicum.filmorate.storage.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class EventService {
    private final UserStorage userStorage;
    private final EventDbStorage eventDbStorage;

    public EventService(UserStorage userStorage, EventDbStorage eventDbStorage) {
        this.userStorage = userStorage;
        this.eventDbStorage = eventDbStorage;
    }

    public List<Event> getEvents(int userId) {
        throwExceptionIfUserDoesNotExist(
                "Выполнена попытка найти пользователя с несуществующим id = {}.",
                userId);

        log.info("Получена история действий пользователя с id = {}.", userId);
        return eventDbStorage.findEventsByUserId(userId);
    }

    public void createEvent(int userId, OperationStatus operation, EventTypeStatus eventType, int entityId) {
        Event eventUser = Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .build();

            eventDbStorage.add(eventUser);
            log.info("История действий пользователя с id = {} сохранена.", userId);
    }

    private void throwExceptionIfUserDoesNotExist(String logMessage, int userId) {
        if (!userStorage.isUserExistsById(userId)) {
            log.warn(logMessage, userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }
    }
}
