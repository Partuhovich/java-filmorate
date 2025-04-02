package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.exeption.ValidationException;

import java.time.LocalDate;

@Data
public class Film {
    private Long id;
    private LocalDate releaseDate;

    @NotBlank(message = "Название не может быть пустым или null")
    private String name;

    @Size(max = 200, message = "Описание не может превышать 200 символов")
    private String description;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private int duration;

    public void validate() {
        if (releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
