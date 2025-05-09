package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
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
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getById(Long id) {
        return users.get(id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        users.get(userId).addFriendId(friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        users.get(userId).removeFriendId(friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        return users.get(userId).getFriendIds().stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        Set<Long> userFriends = users.get(userId).getFriendIds();
        Set<Long> otherFriends = users.get(otherId).getFriendIds();

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
