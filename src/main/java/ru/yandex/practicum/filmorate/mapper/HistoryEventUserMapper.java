package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventTypeStatus;
import ru.yandex.practicum.filmorate.model.EventUser;
import ru.yandex.practicum.filmorate.model.OperationStatus;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class HistoryEventUserMapper implements RowMapper<EventUser> {
    @Override
    public EventUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        int eventId = rs.getInt("event_Id");
        Long timestamp = rs.getLong("timestamp");
        int userId = rs.getInt("user_id");
        int entityId = rs.getInt("entity_id");
        EventTypeStatus eventType = EventTypeStatus.valueOf(rs.getString("event_type"));
        OperationStatus operation = OperationStatus.valueOf(rs.getString("operation"));

        return new EventUser(eventId, timestamp, userId, entityId, eventType, operation);
    }

}
