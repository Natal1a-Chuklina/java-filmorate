package ru.yandex.practicum.filmorate.dto.film;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.validate.StartDateValidation;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class UpdateFilmRequest {
    @NotNull
    private int id;
    @NotBlank
    private String name;
    @Size(max = 200)
    private String description;
    @StartDateValidation(date = "28.12.1895", message = "Дата релиза должна быть позже 28 декабря 1895 года.")
    private LocalDate releaseDate;
    @Positive
    private int duration;
    @Valid
    private Mpa mpa;
    private Set<@Valid Genre> genres;

    public UpdateFilmRequest(int id, String name, String description, LocalDate releaseDate, int duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        genres = new HashSet<>();
    }

    public UpdateFilmRequest() {
        genres = new HashSet<>();
    }
}
