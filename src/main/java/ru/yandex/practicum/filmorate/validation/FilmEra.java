package ru.yandex.practicum.filmorate.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FilmEraValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilmEra {
    String message() default "Invalid release date.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}