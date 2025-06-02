package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    private static final String FIND_ALL_GENRES = "SELECT * FROM genres ORDER BY id";
    private static final String FIND_GENRE_BY_ID = "SELECT * FROM genres WHERE id = ?";

    @Override
    public List<Genre> findAll() {
        return jdbcTemplate.query(FIND_ALL_GENRES, genreRowMapper);
    }

    @Override
    public Optional<Genre> findById(int id) {
        List<Genre> genres = jdbcTemplate.query(FIND_GENRE_BY_ID, genreRowMapper, id);
        return genres.stream().findFirst();
    }

    @Override
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM genres WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
