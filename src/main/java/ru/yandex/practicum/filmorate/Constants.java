package ru.yandex.practicum.filmorate;

public class Constants {
    public static final String USER_NOT_FOUND_MESSAGE = "Пользователь с идентификатором %d не найден";
    public static final String FILM_NOT_FOUND_MESSAGE = "Фильм с идентификатором %d не найден";
    public static final String EMAIL_ALREADY_EXISTS_MESSAGE = "Пользователь с почтой %s уже существует";
    public static final String USERS_ALREADY_FRIENDS_MESSAGE = "Пользователи с id: %d и %d уже являются друзьями";
    public static final String USERS_NOT_FRIENDS_MESSAGE = "Пользователи с id: %d и %d не являются друзьями";
    public static final String USER_ALREADY_LIKED_FILM_MESSAGE = "Пользователь с id = %d уже поставил лайк фильму с id = %d";
    public static final String USER_NOT_LIKED_FILM_MESSAGE = "Пользователь с id = %d не ставил лайк фильму с id = %d";
    public static final String USER_COULD_NOT_ADD_HIMSELF_TO_FRIEND = "Пользователь не может добавить сам себя в друзья";
    public static final String GENRE_NOT_FOUND_MESSAGE = "Жанр с идентификатором %d не найден";
    public static final String RATING_NOT_FOUND_MESSAGE = "Рейтинг с идентификатором %d не найден";
    public static final String UNKNOWN_ERROR_MESSAGE = "Произошла неизвестная ошибка, попробуйте проверить корректность " +
            "всех данных запроса";
    public static final String FILM_ALREADY_EXISTS_MESSAGE = "Фильм с такими именем, описанием, датой релиза и" +
            " длительностью уже существует";
    public static final String REVIEW_NOT_FOUND_MESSAGE = "Отзыв с идентификатором %d не найден";
    public static final String DIRECTOR_NOT_FOUND = "Режиссер с идентификатором %d не найден";
    public static final String USER_ALREADY_LEFT_REVIEW_FOR_FILM_MESSAGE = "Пользователь с id = %d уже оставлял ревью " +
            "фильму с id = %d";
    public static final String USER_ALREADY_LEFT_LIKE_FOR_REVIEW_MESSAGE = "Пользователь с id = %d уже оставлял лайк " +
            "отзыву с id = %d";
    public static final String USER_ALREADY_LEFT_DISLIKE_FOR_REVIEW_MESSAGE = "Пользователь с id = %d уже оставлял дизлайк " +
            "отзыву с id = %d";
    public static final String USER_DID_NOT_LEAVE_LIKE_FOR_REVIEW_MESSAGE = "Пользователь с id = %d не оставлял лайк " +
            "отзыву с id = %d";
    public static final String USER_DID_NOT_LEAVE_DISLIKE_FOR_REVIEW_MESSAGE = "Пользователь с id = %d не оставлял дизлайк " +
            "отзыву с id = %d";

    private Constants() {
    }
}
