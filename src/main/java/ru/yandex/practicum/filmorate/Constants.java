package ru.yandex.practicum.filmorate;

public class Constants {
    public static final String USER_NOT_FOUND_MESSAGE = "Пользователь с идентификатором %d не найден";
    public static final String FILM_NOT_FOUND_MESSAGE = "Фильм с идентификатором %d не найден";
    public static final String USER_ALREADY_EXISTS_MESSAGE = "Пользователь с id = %d уже существует";
    public static final String FILM_ALREADY_EXISTS_MESSAGE = "Фильм с id = %d уже существует";
    public static final String USERS_ALREADY_FRIENDS_MESSAGE = "Пользователи с id: %d и %d уже являются друзьями";
    public static final String USERS_NOT_FRIENDS_MESSAGE = "Пользователи с id: %d и %d не являются друзьями";
    public static final String USER_ALREADY_LIKED_FILM_MESSAGE = "Пользователь с id = %d уже поставил лайк фильму с id = %d";
    public static final String USER_NOT_LIKED_FILM_MESSAGE = "Пользователь с id = %d не ставил лайк фильму с id = %d";
    public static final String USER_COULD_NOT_ADD_HIMSELF_TO_FRIEND = "Пользователь не может добавить сам себя в друзья";

    private Constants() {
    }
}
