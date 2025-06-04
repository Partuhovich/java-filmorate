package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validator.ReleaseDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Film {
    private Long id;
    private final HashSet<Long> likes = new HashSet<>();
    private List<Genre> genres = new ArrayList<>();
    private Mpa mpa;

    @ReleaseDate(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    private LocalDate releaseDate;

    @NotBlank(message = "Название не может быть пустым или null")
    private String name;

    @Size(max = 200, message = "Описание не может превышать 200 символов")
    private String description;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private int duration;

    public void setLikes(Set<Long> likes) {
        this.likes.addAll(likes);
    }

    public void addLike(Long userId) {
        likes.add(userId);
    }

    public void removeLike(Long userId) {
        likes.remove(userId);
    }

}
