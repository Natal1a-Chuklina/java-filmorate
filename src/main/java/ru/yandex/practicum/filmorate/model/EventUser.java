package ru.yandex.practicum.filmorate.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EventUser {
    private int eventId;
    private Long timestamp;
    private int userId;
    private int entityId;
    private EventTypeStatus eventType; //тип события  LIKE, REVIEW или FRIEND
    private OperationStatus operation; //тип действия  REMOVE, ADD, UPDATE
}
