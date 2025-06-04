package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    private static final String FIND_ALL_MPA = "SELECT * FROM mpa_ratings ORDER BY id";
    private static final String FIND_MPA_BY_ID = "SELECT * FROM mpa_ratings WHERE id = ?";

    @Override
    public List<Mpa> findAll() {
        return jdbcTemplate.query(FIND_ALL_MPA, mpaRowMapper);
    }

    @Override
    public Optional<Mpa> findById(int id) {
        List<Mpa> mpaList = jdbcTemplate.query(FIND_MPA_BY_ID, mpaRowMapper, id);
        return mpaList.stream().findFirst();
    }

    @Override
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}
