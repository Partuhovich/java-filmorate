package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeption.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.List;

@Service
public class UserService {
    private final InMemoryUserStorage userStorage;

    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        user.validate();
        return userStorage.create(user);
    }

    public User update(User user) {
        user.validate();
        userStorage.existsById(user.getId());
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getUserById(Long userId) {
        userStorage.existsById(userId);
        return userStorage.getById(userId);
    }

    public void addFriend(Long userId, Long friendId) {
        userStorage.existsById(userId);
        userStorage.existsById(friendId);
        if (userStorage.getFriends(userId).contains(userStorage.getById(friendId))) {
            throw new DuplicatedDataException(
                    String.format("Пользователь с id %d уже в друзьях у пользователя с id %d", friendId, userId)
            );
        }

        userStorage.addFriend(userId, friendId);
        userStorage.addFriend(friendId, userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.existsById(userId);
        userStorage.existsById(friendId);
        userStorage.removeFriend(userId, friendId);
        userStorage.removeFriend(friendId, userId);
    }

    public List<User> getFriends(Long userId) {
        userStorage.existsById(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        userStorage.existsById(userId);
        userStorage.existsById(otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }
}
