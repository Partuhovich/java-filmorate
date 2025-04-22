package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private Map<Long, Film> films = new HashMap<>();
    private Long idCounter = 1L;

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма {}", film.getName());
        film.setId(idCounter++);
        films.put(film.getId(), film);
        log.info("Фильм {} был добавлен", film.getName());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма {}", film.getName());
        Long id = film.getId();
        if (!films.containsKey(id)) {
            String errorMessage = String.format("Фильм с id %d не найден", id);
            log.warn(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        films.replace(id, film);
        log.info("Фильм {} был обновлен", film.getName());
        return film;
    }

    @GetMapping
    public List<Film> getAll() {
        log.info("Получен запрос на получение всех фильмов");
        return new ArrayList<>(films.values());
    }
}
