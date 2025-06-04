package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        existsById(user.getId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getById(Long id) {
        existsById(id);
        return users.get(id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = getById(userId);
        User friend = getById(friendId);

        if (user.getFriends().containsKey(friendId)) {
            throw new DuplicatedDataException(
                    String.format("Пользователь с id %d уже в друзьях у пользователя с id %d", friendId, userId)
            );
        }

        user.addFriend(friendId, false);

        if (friend.getFriends().containsKey(userId)) {
            user.getFriends().put(friendId, true);
            friend.getFriends().put(userId, true);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = getById(userId);
        User friend = getById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        User user = getById(userId);
        return user.getFriends().keySet().stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        Set<Long> userFriends = getById(userId).getFriends().keySet();
        Set<Long> otherFriends = getById(otherId).getFriends().keySet();

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(this::getById)
                .collect(Collectors.toList());
    }

    @Override
    public void existsById(Long userId) {
        if (!users.containsKey(userId)) {
            String errorMessage = String.format("Пользователь с id %d не найден", userId);
            throw new NotFoundException(errorMessage);
        }
    }

    private Long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
