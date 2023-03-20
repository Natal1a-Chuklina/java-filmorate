package ru.yandex.practicum.filmorate.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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
@EqualsAndHashCode
@ToString
@Setter
public class Film {
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
    private Set<Integer> likes;

    public Film(String name, String description, LocalDate releaseDate, int duration, Mpa mpa) {
        this();
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
    }

    public Film(int id, String name, String description, LocalDate releaseDate, int duration, Mpa mpa) {
        this(name, description, releaseDate, duration, mpa);
        this.id = id;
    }

    public Film() {
        likes = new HashSet<>();
        genres = new HashSet<>();
    }

    public boolean addLike(int userId) {
        return likes.add(userId);
    }

    public boolean deleteLike(Integer userId) {
        return likes.remove(userId);
    }

    public boolean addGenre(Genre genre) {
        return genres.add(genre);
    }

    public boolean deleteGenre(Genre genre) {
        return genres.remove(genre);
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
