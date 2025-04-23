package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film create(Film film);
    Film update(Film film);
    List<Film> getAll();
    Film getById(Long id);
    void addLike(Long filmId, Long userId);
    void removeLike(Long filmId, Long userId);
    List<Film> getPopularFilms(int count);
    void existsById(Long id);
    Long getNextId();
}
