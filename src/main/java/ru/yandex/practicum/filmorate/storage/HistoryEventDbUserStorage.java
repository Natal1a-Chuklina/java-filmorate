package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.EventUser;

import java.util.List;

@Repository
@Slf4j
public class HistoryEventDbUserStorage implements HistoryEventUserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<EventUser> historyMapper;

    public HistoryEventDbUserStorage(JdbcTemplate jdbcTemplate, RowMapper<EventUser> historyMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.historyMapper = historyMapper;
    }

    @Override
    public List<EventUser> findHistoryUserById(int userId) {
        String sql = "SELECT * FROM history_user " +
                "WHERE user_id = ?";

        log.info("Получена история действий пользователя с id = {}", userId);
        return jdbcTemplate.query(sql, historyMapper, userId);
    }

    @Override
    public void save(EventUser historyEventUser) {
        String sql = "INSERT INTO history_user(timestamp,user_id,entity_id,event_type,operation) " +
                "VALUES(?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                historyEventUser.getTimestamp(),
                historyEventUser.getUserId(),
                historyEventUser.getEntityId(),
                historyEventUser.getEventType().toString(),
                historyEventUser.getOperation().toString()
        );
        log.info("Данные о действии занесены в историю");
    }
}
