package ru.yandex.practicum.filmorate.model.validate;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StartDateValidator implements ConstraintValidator<StartDateValidation, LocalDate> {
    private LocalDate date;

    @Override
    public void initialize(StartDateValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.date = LocalDate.parse(constraintAnnotation.date(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    @Override
    public boolean isValid(LocalDate s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return false;
        }
        return s.isAfter(date);
    }
}
