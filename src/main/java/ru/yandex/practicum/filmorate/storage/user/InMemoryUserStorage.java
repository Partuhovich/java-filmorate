package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exeption.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Repository
public class InMemoryUserStorage implements UserStorage{
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
    public List<Long> getFriends(Long userId) {
        return new ArrayList<>(users.get(userId).getFriendIds());
    }

    @Override
    public List<Long> getCommonFriends(Long userId, Long otherId) {
        Set<Long> userFriends = users.get(userId).getFriendIds();
        Set<Long> otherFriends = users.get(otherId).getFriendIds();
        Set<Long> commonFriends = new HashSet<>(userFriends);
        commonFriends.retainAll(otherFriends);
        return new ArrayList<>(commonFriends);
    }

    @Override
    public void existsById(Long userId) {
        if (!users.containsKey(userId)) {
            String errorMessage = String.format("Пользователь с id %d не найден", userId);
            throw new NotFoundException(errorMessage);
        }
    }

    @Override
    public Long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
