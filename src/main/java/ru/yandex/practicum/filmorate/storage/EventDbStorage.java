package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Repository
@Slf4j
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Event> eventMapper;

    public EventDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Event> eventMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventMapper = eventMapper;
    }

    @Override
    public List<Event> findEventsByUserId(int userId) {
        String sql = "SELECT * FROM events " +
                "WHERE user_id = ?";

        log.info("Получена история действий пользователя с id = {}", userId);
        return jdbcTemplate.query(sql, eventMapper, userId);
    }

    @Override
    public void add(Event event) {
        String sql = "INSERT INTO events(timestamp,user_id,entity_id,event_type,operation) " +
                "VALUES(?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                event.getTimestamp(),
                event.getUserId(),
                event.getEntityId(),
                event.getEventType().toString(),
                event.getOperation().toString()
        );
        log.info("Данные о действии пользователя с id = {} занесены в историю",event.getUserId());
    }
}
