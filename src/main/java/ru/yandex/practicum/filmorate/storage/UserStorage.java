package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> getAll();

    boolean isUserExist(int userId);

    void add(User user);

    void update(User user);

    User getUser(int userId);
}
