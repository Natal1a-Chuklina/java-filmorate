package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.EventUser;

import java.util.List;

public interface HistoryEventUserStorage {

    List<EventUser> findHistoryUserById(int userId);

    void save(EventUser eventUser);
}
