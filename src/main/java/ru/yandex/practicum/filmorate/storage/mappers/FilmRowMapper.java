package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    private final GenreRowMapper genreRowMapper;

    public FilmRowMapper(GenreRowMapper genreRowMapper) {
        this.genreRowMapper = genreRowMapper;
    }

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")))
                .build();

        List<Genre> genres = getGenresForFilm(film.getId(), rs.getStatement().getConnection());
        film.setGenres(genres);

        Set<Long> likes = getLikesForFilm(film.getId(), rs.getStatement().getConnection());
        film.setLikes(likes);

        return film;
    }

    private List<Genre> getGenresForFilm(Long filmId, java.sql.Connection connection) throws SQLException {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? ORDER BY g.id";

        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, filmId);
            ResultSet rs = stmt.executeQuery();

            List<Genre> genres = new ArrayList<>();
            while (rs.next()) {
                genres.add(genreRowMapper.mapRow(rs, rs.getRow()));
            }
            return genres;
        }
    }

    private Set<Long> getLikesForFilm(Long filmId, java.sql.Connection connection) throws SQLException {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";

        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, filmId);
            ResultSet rs = stmt.executeQuery();

            Set<Long> likes = new HashSet<>();
            while (rs.next()) {
                likes.add(rs.getLong("user_id"));
            }
            return likes;
        }
    }
}
