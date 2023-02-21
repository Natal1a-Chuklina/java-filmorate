package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.Constants.USER_COULD_NOT_ADD_HIMSELF_TO_FRIEND;

@Service
@Slf4j
public class UserService {
    private int counter = 1;
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User createUser(User user) {
        if (userStorage.isUserExist(user.getId())) {
            log.warn("Выполнена попытка добавить пользователя с уже существующим id = {}.", user.getId());
            throw new AlreadyExistException(String.format(Constants.USER_ALREADY_EXISTS_MESSAGE, user.getId()));
        } else {
            user.setId(counter++);
            userStorage.add(user);
            return user;
        }
    }

    public User updateUser(User user) {
        if (!userStorage.isUserExist(user.getId())) {
            log.warn("Выполнена попытка обновить информацию о пользователе с несуществующим id = {}.", user.getId());
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, user.getId()));
        }

        userStorage.update(user);
        return user;
    }

    public User getUserById(int userId) {
        if (!userStorage.isUserExist(userId)) {
            log.warn("Выполнена попытка получить пользователя по несущестующему id = {}", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        return userStorage.getUser(userId);
    }

    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException(USER_COULD_NOT_ADD_HIMSELF_TO_FRIEND);
        }

        if (!userStorage.isUserExist(userId)) {
            log.warn("Выполнена попытка добавить друга пользователю с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        } else if (!userStorage.isUserExist(friendId)) {
            log.warn("Выполнена попытка добавить в друзья пользователя с несуществующим id = {}.", friendId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, friendId));
        }

        if (userStorage.getUser(userId).addFriend(friendId) && userStorage.getUser(friendId).addFriend(userId)) {
            log.info("Пользователь с id = {} добавил в друзья пользователя с id = {}", userId, friendId);
        } else {
            log.warn("Выполнена попытка повторно добваить в друзья пользователю с id = {} пользователя с id = {}",
                    userId, friendId);
            throw new AlreadyExistException(String.format(Constants.USERS_ALREADY_FRIENDS_MESSAGE,
                    userId, friendId));
        }
    }

    public void deleteFriend(int userId, int friendId) {
        if (!userStorage.isUserExist(userId)) {
            log.warn("Выполнена попытка удалить друга у пользователя с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        } else if (!userStorage.isUserExist(friendId)) {
            log.warn("Выполнена попытка удалить из друзей пользователя с несуществующим id = {}.", friendId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, friendId));
        }

        if (userStorage.getUser(userId).deleteFriend(friendId) && userStorage.getUser(friendId).deleteFriend(userId)) {
            log.info("Пользователь с id = {} удалил из друзей пользователя с id = {}", userId, friendId);
        } else {
            log.warn("Выполнена попытка удалить из друзей пользователей, которые не являются друзьями id: {} и {}",
                    userId, friendId);
            throw new AlreadyExistException(String.format(Constants.USERS_NOT_FRIENDS_MESSAGE,
                    userId, friendId));
        }
    }

    public List<User> getFriendsList(int userId) {
        if (!userStorage.isUserExist(userId)) {
            log.warn("Выполнена попытка получить список друзей пользователя с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        log.info("Получен список друзей пользователя с id = {} длиной {}", userId,
                userStorage.getUser(userId).getFriends().size());
        return userStorage.getUser(userId).getFriends().stream().map(userStorage::getUser)
                .collect(Collectors.toList());
    }

    public List<User> getSameFriendsList(int userId, int otherId) {
        if (!userStorage.isUserExist(userId)) {
            log.warn("Выполнена попытка получить список общих друзей пользователя с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        } else if (!userStorage.isUserExist(otherId)) {
            log.warn("Выполнена попытка получить список общих друзей с пользователя с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, otherId));
        }

        log.info("Получен список общих друзей пользователей с id: {} и {}", userId, otherId);
        return userStorage.getUser(userId).getFriends().stream().filter(friendId -> userStorage.getUser(otherId)
                .getFriends().contains(friendId)).map(userStorage::getUser).collect(Collectors.toList());
    }
}
