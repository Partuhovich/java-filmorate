package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User create(User user);

    User update(User user);

    List<User> getAll();

    User getById(Long id);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<Long> getFriends(Long userId);

    List<Long> getCommonFriends(Long userId, Long otherId);

    void existsById(Long id);

    Long getNextId();
}
