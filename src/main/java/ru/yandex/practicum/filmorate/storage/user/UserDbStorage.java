package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users";
    private static final String CREATE_USER_QUERY = "INSERT INTO users (login, name, email, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String CHECK_USER_EXISTS_QUERY = "SELECT COUNT(*) FROM users WHERE id = ?";

    private static final String ADD_FRIEND_QUERY = "INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_ALL_FRIENDS_QUERY = "SELECT friend_id FROM friendship WHERE user_id = ?";
    private static final String CHECK_FRIENDSHIP_EXISTS_QUERY = "SELECT COUNT(*) > 0 FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String UPDATE_FRIENDSHIP_STATUS_QUERY = "UPDATE friendship SET status = ? WHERE user_id = ? AND friend_id = ?";

    public UserDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper userRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    @Override
    public User create(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_USER_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getName());
            ps.setString(3, user.getEmail());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        existsById(user.getId());

        int rowsUpdated = jdbcTemplate.update(
                UPDATE_USER_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );

        return user;
    }

    @Override
    public List<User> getAll() {
        log.info("Получен");
        return jdbcTemplate.query(FIND_ALL_USERS_QUERY, userRowMapper);
    }

    @Override
    public User getById(Long id) {
        existsById(id);
        return jdbcTemplate.queryForObject(FIND_USER_BY_ID_QUERY, userRowMapper, id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        existsById(userId);
        existsById(friendId);

        boolean friendshipExists = Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                CHECK_FRIENDSHIP_EXISTS_QUERY,
                Boolean.class, userId, friendId));

        if (friendshipExists) {
            throw new DuplicatedDataException(
                    String.format("Пользователь с id %d уже в друзьях у пользователя с id %d", friendId, userId)
            );
        }

        jdbcTemplate.update(ADD_FRIEND_QUERY, userId, friendId, false);

        boolean mutualFriendshipExists = Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                CHECK_FRIENDSHIP_EXISTS_QUERY,
                Boolean.class, friendId, userId));

        if (mutualFriendshipExists) {
            jdbcTemplate.update(UPDATE_FRIENDSHIP_STATUS_QUERY, true, userId, friendId);
            jdbcTemplate.update(UPDATE_FRIENDSHIP_STATUS_QUERY, true, friendId, userId);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        existsById(userId);
        existsById(friendId);

        int rowsDeleted = jdbcTemplate.update(DELETE_FRIEND_QUERY, userId, friendId);
        if (rowsDeleted == 0) {
            log.info("Не удалось удалить записи из таблицы friendship");
        } else {
            log.info("Удалено {} строк(и) из таблицы friendship", rowsDeleted);
        }

        jdbcTemplate.update(UPDATE_FRIENDSHIP_STATUS_QUERY, false, friendId, userId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        existsById(userId);
        List<Long> friendsIds = jdbcTemplate.queryForList(
                FIND_ALL_FRIENDS_QUERY,
                Long.class,
                userId);

        return friendsIds.stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        existsById(userId);
        existsById(otherId);

        List<Long> userFriends = jdbcTemplate.queryForList(
                FIND_ALL_FRIENDS_QUERY,
                Long.class,
                userId);

        List<Long> otherFriends = jdbcTemplate.queryForList(
                FIND_ALL_FRIENDS_QUERY,
                Long.class,
                otherId);

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(this::getById)
                .collect(Collectors.toList());
    }

    @Override
    public void existsById(Long userId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_USER_EXISTS_QUERY, Integer.class, userId);
        if (count == null || count == 0) {
            String errorMessage = String.format("Пользователь с id %d не найден", userId);
            throw new NotFoundException(errorMessage);
        }
    }
}
