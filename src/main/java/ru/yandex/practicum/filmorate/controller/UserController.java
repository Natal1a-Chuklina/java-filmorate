package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int counter = 1;

    @GetMapping
    public Collection<User> getAll() {
        log.info("Получен список пользователей длиной {}.", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        if (users.containsKey(user.getId())) {
            log.warn("Выполнена попытка добавить пользователя с уже существующим id = {}.", user.getId());
            throw new AlreadyExistException(String.format("Пользователь с id = %d уже существует", user.getId()));
        } else {
            log.info("Добавлен пользователь с id = {}.", counter);
            user.setId(counter++);
            users.put(user.getId(), user);
            return user;
        }
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            log.warn("Выполнена попытка обновить информацию о пользователе с несуществующим id = {}.", user.getId());
            throw new NotFoundException(String.format("Пользователь с идентификатором %d не найден", user.getId()));
        }

        BeanUtils.copyProperties(user, users.get(user.getId()), "id");
        log.info("Информация о пользователе с id = {} обновлена.", user.getId());
        return user;
    }
}
