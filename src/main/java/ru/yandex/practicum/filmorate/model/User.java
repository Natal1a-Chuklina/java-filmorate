package ru.yandex.practicum.filmorate.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Getter
@EqualsAndHashCode
@ToString
@Setter
public class User {
    private int id;
    @Email
    private String email;
    @NotBlank
    @Pattern(regexp = "^\\S+$")
    private String login;

    private String name;
    @Past
    private LocalDate birthday;
    private Map<Integer, Status> friends;

    public User(String email, String login, String name, LocalDate birthday) {
        this();
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }

    public User(int id, String email, String login, String name, LocalDate birthday) {
        this(email, login, name, birthday);
        this.id = id;
    }

    public User() {
        friends = new HashMap<>();
    }

    public void addFriend(int friendId, Status status) {
        friends.put(friendId, status);
    }

    public void deleteFriend(Integer friendId) {
        friends.remove(friendId);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();

        values.put("email", email);
        values.put("login", login);
        values.put("name", name);
        values.put("birthday", birthday);

        return values;
    }
}
