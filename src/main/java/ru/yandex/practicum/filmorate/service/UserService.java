package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.Constants;
import ru.yandex.practicum.filmorate.dto.user.CreateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserResponse;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Status;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.Constants.USER_COULD_NOT_ADD_HIMSELF_TO_FRIEND;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<UserResponse> getAll() {
        return userStorage.getAll().stream()
                .map(this::createUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse createUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday());
    }

    private UserResponse createUserResponse(CreateUserRequest user, int userId) {
        return new UserResponse(userId, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday());
    }

    private UserResponse createUserResponse(UpdateUserRequest user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday());
    }

    public UserResponse createUser(CreateUserRequest user) {
        checkUserName(user);

        if (userStorage.isUserExistsByEmail(user.getEmail())) {
            log.warn("Выполнена попытка добавить пользователя с уже существующей почтой {}.", user.getEmail());
            throw new AlreadyExistException(String.format(Constants.EMAIL_ALREADY_EXISTS_MESSAGE, user.getEmail()));
        } else {
            int id = userStorage.add(user);
            return createUserResponse(user, id);
        }
    }

    private void checkUserName(CreateUserRequest user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public UserResponse updateUser(UpdateUserRequest user) {
        if (!userStorage.isUserExistsById(user.getId())) {
            log.warn("Выполнена попытка обновить информацию о пользователе с несуществующим id = {}.", user.getId());
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, user.getId()));
        }

        userStorage.update(user);
        return createUserResponse(user);
    }

    public UserResponse getUserById(int userId) {
        if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка получить пользователя по несущестующему id = {}", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        User user = userStorage.getUser(userId);

        return createUserResponse(user);
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

    public List<UserResponse> getFriendsList(int userId) {
        if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка получить список друзей пользователя с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        }

        return userStorage.getFriends(userId).stream()
                .map(this::createUserResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getSameFriendsList(int userId, int otherId) {
        if (!userStorage.isUserExistsById(userId)) {
            log.warn("Выполнена попытка получить список общих друзей пользователя с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, userId));
        } else if (!userStorage.isUserExistsById(otherId)) {
            log.warn("Выполнена попытка получить список общих друзей с пользователя с несуществующим id = {}.", userId);
            throw new NotFoundException(String.format(Constants.USER_NOT_FOUND_MESSAGE, otherId));
        }

        return userStorage.getCommonFriends(userId, otherId).stream()
                .map(this::createUserResponse)
                .collect(Collectors.toList());
    }
}
