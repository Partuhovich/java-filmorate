package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Slf4j
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    public List<Mpa> getAllMpaRatings() {
        log.info("Получен запрос на получение всех рейтингов MPA");
        return mpaService.getAllMpaRatings();
    }

    @GetMapping("/{id}")
    public Mpa getMpaRatingById(@PathVariable int id) {
        log.info("Получен запрос на получение рейтинга MPA с ID {}", id);
        return mpaService.getMpaRatingById(id);
    }
}