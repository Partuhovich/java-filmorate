package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, @Qualifier("userDbStorage") UserStorage userStorage, MpaStorage mpaStorage, GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Film create(Film film) {
        validateFilm(film);
        Film createdFilm = filmStorage.create(film);
        log.info("Создан новый фильм: {}", createdFilm);
        return createdFilm;
    }

    public Film update(Film film) {
        validateFilm(film);
        filmStorage.existsById(film.getId());
        Film updatedFilm = filmStorage.update(film);
        log.info("Обновлен фильм: {}", updatedFilm);
        return updatedFilm;
    }

    public List<Film> getAll() {
        List<Film> films = filmStorage.getAll();
        log.info("Получено {} фильмов", films.size());
        return films;
    }

    public Film getFilmById(Long filmId) {
        Film film = filmStorage.getById(filmId);
        log.info("Получен фильм с ID {}: {}", filmId, film);
        return film;
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.existsById(filmId);
        userStorage.existsById(userId);

        if (filmStorage.likeExists(filmId, userId)) {
            throw new DuplicatedDataException(
                    String.format("Пользователь с id %d уже оставлял лайк на фильм с id %d", userId, filmId)
            );
        }

        filmStorage.addLike(filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.existsById(filmId);
        userStorage.existsById(userId);

        if (!filmStorage.likeExists(filmId, userId)) {
            throw new NotFoundException(
                    String.format("Пользователь с id %d не оставлял лайк на фильм с id %d", userId, filmId)
            );
        }

        filmStorage.removeLike(filmId, userId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(Integer count) {
        int actualCount = count == null || count <= 0 ? 10 : count;
        List<Film> popularFilms = filmStorage.getPopularFilms(actualCount);
        log.info("Получено {} популярных фильмов", popularFilms.size());
        return popularFilms;
    }

    private void validateFilm(Film film) {
        if (!mpaStorage.existsById(film.getMpa().getId())) {
            throw new NotFoundException("MPA с ID " + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> {
                if (!genreStorage.existsById(genre.getId())) {
                    throw new NotFoundException("Жанр с ID " + genre.getId() + " не найден");
                }
            });
        }
    }
}
