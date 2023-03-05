package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users;

    public InMemoryUserStorage() {
        users = new HashMap<>();
    }

    @Override
    public Collection<User> getAll() {
        log.info("Получен список пользователей длиной {}.", users.size());
        return users.values();
    }

    @Override
    public boolean isUserExist(int userId) {
        return users.containsKey(userId);
    }

    @Override
    public void add(User user) {
        users.put(user.getId(), user);
        log.info("Добавлен пользователь с id = {}.", user.getId());
    }

    @Override
    public void update(User user) {
        BeanUtils.copyProperties(user, users.get(user.getId()), "id");
        log.info("Информация о пользователе с id = {} обновлена.", user.getId());
    }

    @Override
    public User getUser(int userId) {
        return users.get(userId);
    }
}
