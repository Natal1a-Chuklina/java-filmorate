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
import java.util.HashSet;
import java.util.Set;

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
    private final Set<Integer> friends;

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        initName(login, name);
        this.birthday = birthday;
        friends = new HashSet<>();
    }

    private void initName(String login, String name) {
        this.name = (name == null || name.isBlank()) ? login : name;
    }

    public boolean addFriend(int friendId) {
        return friends.add(friendId);
    }

    public boolean deleteFriend(Integer friendId) {
        return friends.remove(friendId);
    }
}
