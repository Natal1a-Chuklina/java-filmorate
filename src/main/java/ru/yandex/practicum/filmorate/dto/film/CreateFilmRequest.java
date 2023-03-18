package ru.yandex.practicum.filmorate.dto.film;

import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.validate.StartDateValidation;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class CreateFilmRequest {
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

    public CreateFilmRequest(String name, String description, LocalDate releaseDate, int duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        genres = new HashSet<>();
    }

    public CreateFilmRequest() {
        genres = new HashSet<>();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();

        values.put("name", name);
        values.put("description", description);
        values.put("release_date", releaseDate);
        values.put("duration", duration);
        values.put("rating_id", mpa.getId());

        return values;
    }
}
