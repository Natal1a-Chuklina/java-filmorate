package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.EventUser;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.HistoryService;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final HistoryService historyService;

    public UserController(UserService userService, HistoryService historyService) {
        this.userService = userService;
        this.historyService = historyService;
    }

    @GetMapping
    public Collection<User> getAll() {
        log.info("Попытка получить список пользователей");
        return userService.getAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User createUserRequest) {
        log.info("Попытка создать пользователя: {}", createUserRequest);
        return userService.createUser(createUserRequest);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Попытка обновить информацию о пользователе: {}", user);
        return userService.updateUser(user);
    }

    @GetMapping("/{userId}")
    public User getUserById(@PathVariable int userId) {
        log.info("Попытка получить информацию о пользователе с id = {}", userId);
        return userService.getUserById(userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable int userId) {
        log.info("Получен запрос на удаление пользователя с id = {}", userId);
        userService.deleteUser(userId);
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
    public List<User> getFriendsList(@PathVariable int userId) {
        log.info("Попытка получить список пользователей с id = {}", userId);
        return userService.getFriendsList(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public List<User> getSameFriendsList(@PathVariable int userId, @PathVariable int otherId) {
        log.info("Попытка получить список общих друзей пользователей с id: {} и {}", userId, otherId);
        return userService.getSameFriendsList(userId, otherId);
    }

    @GetMapping("/{id}/feed")
    public Collection<EventUser> getHistoryByUserId(@PathVariable int id) {
        log.info("Попытка получить историю пользователя с id = {}", id);
        return historyService.getHistoryUser(id);
    }
}
