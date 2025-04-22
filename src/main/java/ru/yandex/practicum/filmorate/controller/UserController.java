package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private Map<Long, User> users = new HashMap<>();
    private Long idCounter = 1L;

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        user.validate();
        log.info("Получен запрос на добавление пользователя {}", user.getLogin());
        user.setId(idCounter++);
        users.put(user.getId(), user);
        log.info("Пользователь {} был добавлен", user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        user.validate();
        log.info("Получен запрос на обновление пользователя {}", user.getLogin());
        Long id = user.getId();
        if (!users.containsKey(id)) {
            String errorMessage = String.format("Пользователь с id %d не найден", id);
            log.warn(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        users.replace(id, user);
        log.info("Пользователь {} был обновлен", user.getLogin());
        return user;
    }

    @GetMapping
    public List<User> getAll() {
        log.info("Получен запрос на получение всех пользователей");
        return new ArrayList<>(users.values());
    }
}
