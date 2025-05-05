package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.List;

@Service
public class FilmService {
    private final InMemoryFilmStorage filmStorage;
    private final InMemoryUserStorage userStorage;

    public FilmService(InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        filmStorage.existsById(film.getId());
        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getFilmById(Long filmId) {
        filmStorage.existsById(filmId);
        return filmStorage.getById(filmId);
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.existsById(filmId);
        userStorage.existsById(userId);

        if (filmStorage.getById(filmId).getLikes().contains(userId)) {
            throw new DuplicatedDataException(
                    String.format("Пользователь с id %d уже оставлял лайк на фильм с id %d", userId, filmId)
            );
        }

        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.existsById(filmId);
        userStorage.existsById(userId);

        if (!filmStorage.getById(filmId).getLikes().contains(userId)) {
            throw new NotFoundException(
                    String.format("Пользователь с id %d не оставлял лайк на фильм с id %d", userId, filmId)
            );
        }

        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(Integer count) {
        int actualCount = count == null ? 10 : count;
        return filmStorage.getPopularFilms(actualCount);
    }
}
