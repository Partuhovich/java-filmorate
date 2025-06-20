package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private Long id;
    private final Map<Long, Boolean> friends = new HashMap<>();

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть корректным адресом электронной почты")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    public void addFriend(Long friendId, Boolean status) {
        friends.put(friendId, status);
    }

    public void removeFriend(Long id) {
        friends.remove(id);
    }

    public void validate() {
        if (name == null || name.isBlank()) {
            name = login;
        }
    }
}