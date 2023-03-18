package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.dto.user.CreateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.model.Status;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> getAll();

    boolean isUserExistsByEmail(String email);

    boolean isUserExistsById(int id);

    boolean isUserContainsFriend(int userId, int friendId);

    int add(CreateUserRequest user);

    void update(UpdateUserRequest user);

    User getUser(int userId);

    void addFriend(int userId, int friendId, Status status);

    void deleteFriend(int userId, int friendId);

    Collection<User> getFriends(int userId);

    Collection<User> getCommonFriends(int userId, int otherId);
}
