package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Status;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ru.yandex.practicum.filmorate.Constants.USER_COULD_NOT_ADD_HIMSELF_TO_FRIEND;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User createUser(User user) {
        checkUserName(user);

        if (userStorage.isUserExistsByEmail(user.getEmail())) {
            log.warn("Выполнена попытка добавить пользователя с уже существующей почтой {}.", user.getEmail());
            throw new AlreadyExistException(String.format(Constants.EMAIL_ALREADY_EXISTS_MESSAGE, user.getEmail()));
        } else {
            return userStorage.getUser(userStorage.add(user));
        }
    }

    private void checkUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public User updateUser(User user) {
        if (!userStorage.isUserExistsById(user.getId())) {
            log.warn("Выполнена попытка обновить информацию о пользователе с несуществующим id = {}.", user.getId());
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, user.getId()));
        }

        userStorage.update(user);
        return userStorage.getUser(user.getId());
    }

    public User getUserById(int userId) {
        if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка получить пользователя по несущестующему id = {}", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        return userStorage.getUser(userId);
    }

    public void deleteUser(int userId) {
        if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка удалить пользователя по несущестующему id = {}", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        userStorage.delete(userId);
    }

    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException(USER_COULD_NOT_ADD_HIMSELF_TO_FRIEND);
        }

        if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка добавить друга пользователю с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        } else if (!userStorage.isUserExistsById(friendId)) {
            log.warn("Выполнена попытка добавить в друзья пользователя с несуществующим id = {}.", friendId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, friendId));
        }

        if (userStorage.isUserContainsFriend(userId, friendId)) {
            log.warn("Выполнена попытка повторно добваить в друзья пользователю с id = {} пользователя с id = {}",
                    userId, friendId);
            throw new AlreadyExistException(String.format(Constants.USERS_ALREADY_FRIENDS_MESSAGE,
                    userId, friendId));
        } else {
            userStorage.addFriend(userId, friendId, Status.CONFIRMED);
            userStorage.addFriend(friendId, userId, Status.UNCONFIRMED);
        }
    }

    public void deleteFriend(int userId, int friendId) {
        if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка удалить друга у пользователя с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        } else if (!userStorage.isUserExistsById(friendId)) {
            log.warn("Выполнена попытка удалить из друзей пользователя с несуществующим id = {}.", friendId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, friendId));
        }

        if (userStorage.isUserContainsFriend(userId, friendId)) {
            userStorage.deleteFriend(userId, friendId);
        } else {
            log.warn("Выполнена попытка удалить из друзей пользователей, которые не являются друзьями id: {} и {}",
                    userId, friendId);
            throw new AlreadyExistException(String.format(Constants.USERS_NOT_FRIENDS_MESSAGE,
                    userId, friendId));
        }
    }

    public List<User> getFriendsList(int userId) {
        if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка получить список друзей пользователя с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        return new ArrayList<>(userStorage.getFriends(userId));
    }

    public List<User> getSameFriendsList(int userId, int otherId) {
        if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка получить список общих друзей пользователя с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        } else if (!userStorage.isUserExistsById(otherId)) {
            log.warn("Выполнена попытка получить список общих друзей с пользователя с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, otherId));
        }

        return new ArrayList<>(userStorage.getCommonFriends(userId, otherId));
    }
}
