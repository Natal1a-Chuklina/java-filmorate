package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class Director {

    private long id;
    @NotBlank
    private String name;

    public Director(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
