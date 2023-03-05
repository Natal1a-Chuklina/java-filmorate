package ru.yandex.practicum.filmorate.model.validate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StartDateValidatorTest {
    private final static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void testValidationWhenDateIsCorrect() {
        Film film = new Film("name", "description", LocalDate.of(1999, 3, 31),
                120);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(0, violations.size());
    }

    @Test
    void testValidationWhenDateIsIncorrect() {
        Film film = new Film("name", "description", LocalDate.of(1799, 3, 31),
                120);
        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertEquals(1, violations.size());
    }
}