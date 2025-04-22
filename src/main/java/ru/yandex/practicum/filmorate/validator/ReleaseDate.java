package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = AnnotationValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReleaseDate {
    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}