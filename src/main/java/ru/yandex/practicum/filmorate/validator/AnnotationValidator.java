package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class AnnotationValidator implements ConstraintValidator<ReleaseDate, LocalDate> {

    @Override
    public boolean isValid(LocalDate release, ConstraintValidatorContext context) {
        LocalDate firstFilmDate = LocalDate.of(1895, 12, 28);
        return release.isEqual(firstFilmDate) || release.isAfter(firstFilmDate);
    }
}
