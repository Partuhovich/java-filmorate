package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    private static final String FIND_ALL_FILMS = "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_id = m.id";
    private static final String FIND_FILM_BY_ID = "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";
    private static final String CREATE_FILM = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    private static final String ADD_LIKE = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_POPULAR_FILMS = "SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) AS likes_count " +
            "FROM films f JOIN mpa_ratings m ON f.mpa_id = m.id " +
            "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
            "GROUP BY f.id ORDER BY likes_count DESC LIMIT ?";
    private static final String CHECK_FILM_EXISTS = "SELECT COUNT(*) FROM films WHERE id = ?";
    private static final String CHECK_LIKE_EXISTS = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";

    @Override
    public List<Film> getAll() {
        return jdbcTemplate.query(FIND_ALL_FILMS, filmRowMapper);
    }

    @Override
    public Film getById(Long id) {
        List<Film> films = jdbcTemplate.query(FIND_FILM_BY_ID, filmRowMapper, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }
        return films.get(0);
    }

    @Override
    public Film create(Film film) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveFilmGenres(filmId, film.getGenres());
        }

        return film;
    }

    private void saveFilmGenres(Long filmId, List<Genre> genres) {
        // Удаляем старые жанры
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);

        // Сохраняем уникальные жанры (без дубликатов в БД)
        Set<Integer> addedGenreIds = new HashSet<>();
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        for (Genre genre : genres) {
            if (!addedGenreIds.contains(genre.getId())) {
                jdbcTemplate.update(sql, filmId, genre.getId());
                addedGenreIds.add(genre.getId());
            }
        }
    }

    @Override
    public Film update(Film film) {
        int rowsUpdated = jdbcTemplate.update(
                UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        if (rowsUpdated == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }
        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(ADD_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        int rowsDeleted = jdbcTemplate.update(REMOVE_LIKE, filmId, userId);
        if (rowsDeleted == 0) {
            throw new NotFoundException("Лайк не найден");
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return jdbcTemplate.query(GET_POPULAR_FILMS, filmRowMapper, count);
    }

    @Override
    public void existsById(Long filmId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_FILM_EXISTS, Integer.class, filmId);
        if (count == null || count == 0) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
    }

    @Override
    public boolean likeExists(Long filmId, Long userId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_LIKE_EXISTS, Integer.class, filmId, userId);
        return count != null && count > 0;
    }
}
