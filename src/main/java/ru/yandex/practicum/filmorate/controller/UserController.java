package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.user.CreateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UserResponse;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<UserResponse> getAll() {
        log.info("Попытка получить список пользователей");
        return userService.getAll();
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest createUserRequest) {
        log.info("Попытка создать пользователя: {}", createUserRequest);
        return userService.createUser(createUserRequest);
    }

    @PutMapping
    public UserResponse update(@Valid @RequestBody UpdateUserRequest user) {
        log.info("Попытка обновить информацию о пользователе: {}", user);
        return userService.updateUser(user);
    }

    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable int userId) {
        log.info("Попытка получить информацию о пользователе с id = {}", userId);
        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(@PathVariable int userId, @PathVariable int friendId) {
        log.info("Попытка добавить в друзья пользователю с id = {} пользователя с id = {}", userId, friendId);
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void deleteFriend(@PathVariable int userId, @PathVariable int friendId) {
        log.info("Попытка удалить из друзей пользователя с id = {} пользователя с id = {}", userId, friendId);
        userService.deleteFriend(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public List<UserResponse> getFriendsList(@PathVariable int userId) {
        log.info("Попытка получить список пользователей с id = {}", userId);
        return userService.getFriendsList(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public List<UserResponse> getSameFriendsList(@PathVariable int userId, @PathVariable int otherId) {
        log.info("Попытка получить список общих друзей пользователей с id: {} и {}", userId, otherId);
        return userService.getSameFriendsList(userId, otherId);
    }
}
