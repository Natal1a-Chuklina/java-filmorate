package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Status;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Repository
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userMapper;

    public UserDbStorage(JdbcTemplate jdbcTemplate, RowMapper<User> userMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
    }

    @Override
    public Collection<User> getAll() {
        String sql =
                "SELECT u.id, " +
                        "       u.email, " +
                        "       u.login, " +
                        "       u.name, " +
                        "       u.birthday, " +
                        "       array_agg(f.friend_2_id || ' ' || s.name) AS friends_data " +
                        "FROM users AS u " +
                        "LEFT JOIN friends AS f ON f.friend_1_id = u.id " +
                        "LEFT JOIN statuses AS s ON s.id = f.status_id " +
                        "GROUP BY u.id;";

        log.info("Получен список всех пользователей из базы");
        return jdbcTemplate.query(sql, userMapper);
    }

    @Override
    public boolean isUserExistsByEmail(String email) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE email = ?;", email);

        log.info("Получена информация о наличии пользователя с email = {} в базе", email);
        return rowSet.next();
    }

    @Override
    public boolean isUserExistsById(int id) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT * FROM users WHERE id = ?;", id);

        log.info("Получена информация о наличии пользователя с id = {} в базе", id);
        return rowSet.next();
    }

    @Override
    public boolean isUserContainsFriend(int userId, int friendId) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(
                "SELECT * " +
                        "FROM friends AS f " +
                        "LEFT JOIN statuses AS s ON f.status_id = s.id " +
                        "WHERE f.friend_1_id = ? " +
                        "  AND f.friend_2_id = ? " +
                        "  AND s.name = 'Confirmed';", userId, friendId);

        log.info("Получена информация находится ли пользоавтель с id = {} в друзьях у пользователя с id = {}",
                friendId, userId);
        return rowSet.next();
    }

    @Override
    public int add(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        int userId = simpleJdbcInsert.executeAndReturnKey(user.toMap()).intValue();

        log.info("В базу добавлен пользователь с id = {}", userId);
        return userId;
    }

    @Override
    public void update(User user) {
        String sql =
                "UPDATE users " +
                        "SET email = ?, " +
                        "    login = ?, " +
                        "    name = ?, " +
                        "    birthday = ? " +
                        "WHERE id = ?;";

        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        log.info("Информация о пользователе с id = {} обновлена в базе", user.getId());
    }

    @Override
    public User getUser(int userId) {
        String sql =
                "SELECT u.id, " +
                        "       u.email, " +
                        "       u.login, " +
                        "       u.name, " +
                        "       u.birthday, " +
                        "       array_agg(f.friend_2_id || ' ' || s.name) AS friends_data " +
                        "FROM users AS u " +
                        "LEFT JOIN friends AS f ON f.friend_1_id = u.id " +
                        "LEFT JOIN statuses AS s ON s.id = f.status_id " +
                        "WHERE u.id = ? " +
                        "GROUP BY u.id;";

        log.info("Пользователь с id = {} получен из базы", userId);
        return jdbcTemplate.queryForObject(sql, userMapper, userId);
    }

    @Override
    public void delete(int userId) {
        final String sqlQuery = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sqlQuery, userId);
        log.info("Пользователь с id {} удален", userId);
    }

    @Override
    public void addFriend(int userId, int friendId, Status status) {
        if (status == null) {
            return;
        }
        String statusName = (status.equals(Status.CONFIRMED) ? "Confirmed" : "Unconfirmed");

        String sql =
                "INSERT INTO friends (friend_1_id, friend_2_id, status_id)" +
                        "VALUES (?, ?, (SELECT id FROM statuses WHERE name = ?));";

        jdbcTemplate.update(sql, userId, friendId, statusName);
        log.info("Пользователю с id = {} добавлен в друзья пользователь с id = {} со статусом дружбы: {}",
                userId, friendId, statusName);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        String sql =
                "DELETE " +
                        "FROM friends " +
                        "WHERE friend_1_id = ?" +
                        "  AND friend_2_id = ?;";

        jdbcTemplate.update(sql, userId, friendId);
        log.info("У пользователя с id = {} был удален из друзей пользователь с id = {}", userId, friendId);
    }

    @Override
    public Collection<User> getFriends(int userId) {
        String sql =
                "SELECT u.id, " +
                        "       u.name, " +
                        "       u.login, " +
                        "       u.email, " +
                        "       u.birthday, " +
                        "       array_agg(f.friend_2_id || ' ' || s.name) AS friends_data " +
                        "FROM users AS u " +
                        "LEFT JOIN friends AS f ON f.friend_1_id = u.id " +
                        "LEFT JOIN statuses AS s ON s.id = f.status_id " +
                        "WHERE u.id in " +
                        "    (SELECT f.friend_2_id " +
                        "     FROM friends AS f " +
                        "     LEFT JOIN statuses AS s ON s.id = f.status_id " +
                        "     WHERE f.friend_1_id = ? " +
                        "       AND s.name = 'Confirmed') " +
                        "GROUP BY u.id;";

        log.info("Получен из базы список друзей пользователя с id = {}", userId);
        return jdbcTemplate.query(sql, userMapper, userId);
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        String sql =
                "SELECT u.id, " +
                        "       u.name, " +
                        "       u.login, " +
                        "       u.email, " +
                        "       u.birthday, " +
                        "       array_agg(f.friend_2_id || ' ' || s.name) AS friends_data " +
                        "FROM users AS u " +
                        "LEFT JOIN friends AS f ON f.friend_1_id = u.id " +
                        "LEFT JOIN statuses AS s ON s.id = f.status_id " +
                        "WHERE u.id in " +
                        "    (SELECT f_1.friend_2_id " +
                        "     FROM friends AS f_1 " +
                        "     LEFT JOIN statuses AS s ON s.id = f_1.status_id " +
                        "     INNER JOIN " +
                        "       (SELECT friend_2_id " +
                        "        FROM friends AS f " +
                        "        LEFT JOIN statuses AS s ON s.id = f.status_id " +
                        "        WHERE friend_1_id = ? " +
                        "          AND s.name = 'Confirmed' ) AS f_2 ON f_2.friend_2_id = f_1.friend_2_id " +
                        "     WHERE f_1.friend_1_id = ? " +
                        "       AND s.name = 'Confirmed') " +
                        "GROUP BY u.id;";

        log.info("Получен из базы список общих друзей пользователей с id: {} и {}", userId, otherId);
        return jdbcTemplate.query(sql, userMapper, userId, otherId);
    }

    @Override
    public Collection<User> getSimilarInterestUsers(int userId) {
        String sql = 
                        "SELECT u.id, " +
                        "       u.name, " +
                        "       u.login, " +
                        "       u.email, " +
                        "       u.birthday, " +
                        "       array_agg(f.friend_2_id || ' ' || s.name) AS friends_data " +
                        "FROM users AS u " +
                        "         LEFT JOIN friends AS f ON f.friend_1_id = u.id " +
                        "         LEFT JOIN statuses AS s ON s.id = f.status_id " +
                        "WHERE u.id IN ( " +
                        "    SELECT user_id FROM ( " +
                        "        SELECT user_id, COUNT(1) AS CNT FROM likes " +
                        "        WHERE user_id <> ? " +
                        "          AND film_id IN ( " +
                        "            select film_id from LIKES where user_id = ? " +
                        "            ) " +
                        "        GROUP BY user_id " +
                        "        HAVING CNT=( " +
                        "            SELECT COUNT(1) FROM likes " +
                        "            WHERE film_id IN ( " +
                        "                select film_id from likes " +
                        "                where user_id = ? " +
                        "                ) " +
                        "            GROUP BY user_id " +
                        "            ORDER BY CNT LIMIT 1) " +
                        "        ORDER BY CNT DESC " +
                        "        ) " +
                        "    ) " +
                        "GROUP BY u.id;";

        log.info("Получен список всех пользователей со схожими интересами с пользователем с id = {}", userId);
        return jdbcTemplate.query(sql, userMapper, userId, userId, userId);
    }
}
