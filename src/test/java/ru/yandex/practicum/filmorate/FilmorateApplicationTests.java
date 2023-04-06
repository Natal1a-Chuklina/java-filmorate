package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;

    private int createUserInDb(String email, String login, String name, LocalDate birthday) {
        User correctAddUserRequest = new User(
                email,
                login,
                name,
                birthday
        );

        return userStorage.add(correctAddUserRequest);
    }

    @Test
    void testGettingAllUsersOfEmptyDb() {
        assertThatCode(() -> {
            Collection<User> allUsersFromDb = userStorage.getAll();

            assertThat(allUsersFromDb)
                    .as("Проверка получения пустого списка пользователей при пустой бд")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingAllUsers() {
        String email1 = "email1@mail.ru";
        String login1 = "login1";
        String name1 = "name1";
        LocalDate birthday1 = LocalDate.now();

        String email2 = "email2@mail.ru";
        String login2 = "login2";
        String name2 = "name2";
        LocalDate birthday2 = LocalDate.now();

        assertThatCode(() -> {
            int user1IdAdded = createUserInDb(email1, login1, name1, birthday1);
            int user2IdAdded = createUserInDb(email2, login2, name2, birthday2);

            Collection<User> allUsersFromDb = userStorage.getAll();

            assertThat(allUsersFromDb)
                    .as("Проверка получения непустого списка пользователей при непустой бд")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(new User(user1IdAdded, email1, login1, name1, birthday1))
                    .contains(new User(user2IdAdded, email2, login2, name2, birthday2));
        }).doesNotThrowAnyException();
    }

    @Test
    void testGettingUserExistenceByEmailWhenUserDoesNotExist() {
        assertThatCode(() -> {
            String email = "email@mail.ru";
            boolean isUserExist = userStorage.isUserExistsByEmail(email);

            assertThat(isUserExist)
                    .as("Проверка получения false при отсутсвии пользователя в бд")
                    .isFalse();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingUserExistenceByEmailWhenUserExists() {
        String email = "email@mail.ru";
        String login = "login";
        String name = "name";
        LocalDate birthday = LocalDate.now();

        assertThatCode(() -> {
            createUserInDb(email, login, name, birthday);
            boolean isUserExist = userStorage.isUserExistsByEmail(email);

            assertThat(isUserExist)
                    .as("Проверка получения true, когда пользователь с таким email есть в бд")
                    .isTrue();
        }).doesNotThrowAnyException();
    }

    @Test
    void testGettingUserExistenceByIdWhenUserDoesNotExist() {
        assertThatCode(() -> {
            int id = 1;
            boolean isUserExist = userStorage.isUserExistsById(id);

            assertThat(isUserExist)
                    .as("Проверка получения false при отсутсвии пользователя в бд")
                    .isFalse();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingUserExistenceByIdWhenUserExists() {
        String email = "email@mail.ru";
        String login = "login";
        String name = "name";
        LocalDate birthday = LocalDate.now();

        assertThatCode(() -> {
            int userIdAdded = createUserInDb(email, login, name, birthday);
            boolean isUserExist = userStorage.isUserExistsById(userIdAdded);

            assertThat(isUserExist)
                    .as("Проверка получения true, когда пользователь с таким id есть в бд")
                    .isTrue();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingFriendExistenceWhenFriendDoesNotExist() {
        String email1 = "email1@mail.ru";
        String login1 = "login1";
        String name1 = "name1";
        LocalDate birthday1 = LocalDate.now();

        String email2 = "email2@mail.ru";
        String login2 = "login2";
        String name2 = "name2";
        LocalDate birthday2 = LocalDate.now();

        assertThatCode(() -> {
            int user1IdAdded = createUserInDb(email1, login1, name1, birthday1);
            int user2IdAdded = createUserInDb(email2, login2, name2, birthday2);
            boolean isFriendExist = userStorage.isUserContainsFriend(user1IdAdded, user2IdAdded);

            assertThat(isFriendExist)
                    .as("Проверка получения false при отсутсвии друга в бд")
                    .isFalse();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingFriendExistenceWhenFriendExists() {
        String email1 = "email1@mail.ru";
        String login1 = "login1";
        String name1 = "name1";
        LocalDate birthday1 = LocalDate.now();

        String email2 = "email2@mail.ru";
        String login2 = "login2";
        String name2 = "name2";
        LocalDate birthday2 = LocalDate.now();

        assertThatCode(() -> {
            int user1IdAdded = createUserInDb(email1, login1, name1, birthday1);
            int user2IdAdded = createUserInDb(email2, login2, name2, birthday2);
            userStorage.addFriend(user1IdAdded, user2IdAdded, Status.CONFIRMED);

            boolean isFriendExist = userStorage.isUserContainsFriend(user1IdAdded, user2IdAdded);

            assertThat(isFriendExist)
                    .as("Проверка получения true при наличии друга в бд")
                    .isTrue();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testAddingAndGettingOfCorrectUser() {
        String email = "email@mail.ru";
        String login = "login";
        String name = "name";
        LocalDate birthday = LocalDate.now();

        assertThatCode(() -> {
            int userIdAdded = createUserInDb(email, login, name, birthday);

            assertThat(userIdAdded)
                    .as("Проверка получения id добавленного пользователя в бд")
                    .isPositive();

            User userFromDb = userStorage.getUser(userIdAdded);

            assertThat(userFromDb)
                    .as("Проверка корректности получения пользователя из бд")
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("id", userIdAdded)
                    .hasFieldOrPropertyWithValue("email", email)
                    .hasFieldOrPropertyWithValue("login", login)
                    .hasFieldOrPropertyWithValue("name", name)
                    .hasFieldOrPropertyWithValue("birthday", birthday);

        }).doesNotThrowAnyException();
    }

    static Stream<User> wrongUserRequestsStream() {
        User nullEmailRequest
                = new User(null, "login", "name", LocalDate.now());
        User nullLoginRequest
                = new User("email", null, "name", LocalDate.now());
        User nullNameRequest
                = new User("email", "login", null, LocalDate.now());
        User birthdayInFutureRequest
                = new User("email", "login", "name", LocalDate.of(5000, 1, 1));

        return Stream.of(nullEmailRequest, nullLoginRequest, nullNameRequest, birthdayInFutureRequest);

    }

    @ParameterizedTest
    @MethodSource("wrongUserRequestsStream")
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testAddingOfIncorrectUser(User request) {
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .as("Проверка добавления пользователя с некорректными данными. %s", request)
                .isThrownBy(() -> userStorage.add(request));

    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testCorrectUserUpdating() {
        String oldEmail = "old@mail.ru";
        String oldLogin = "oldLogin";
        String oldName = "oldName";
        LocalDate oldDate = LocalDate.now();

        int userId = createUserInDb(oldEmail, oldLogin, oldName, oldDate);

        String newEmail = "new@mail.ru";
        String newLogin = "newLogin";
        String newName = "newName";
        LocalDate newDate = LocalDate.now();

        assertThat(userStorage.getUser(userId))
                .as("Проверка корректности заполнения данными пользователя")
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", userId)
                .hasFieldOrPropertyWithValue("email", oldEmail)
                .hasFieldOrPropertyWithValue("login", oldLogin)
                .hasFieldOrPropertyWithValue("name", oldName)
                .hasFieldOrPropertyWithValue("birthday", oldDate);

        User updateUserRequest = new User(userId, newEmail, newLogin, newName, newDate);

        assertThatCode(() -> userStorage.update(updateUserRequest))
                .doesNotThrowAnyException();

        User userWithNewData = userStorage.getUser(userId);

        assertThat(userWithNewData)
                .as("Проверка корректности обновления данных")
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", userId)
                .hasFieldOrPropertyWithValue("email", newEmail)
                .hasFieldOrPropertyWithValue("login", newLogin)
                .hasFieldOrPropertyWithValue("name", newName)
                .hasFieldOrPropertyWithValue("birthday", newDate);
    }

    static Stream<User> wrongUpdateRequestsStream() {
        User nullEmailRequest
                = new User(0, null, "login", "name", LocalDate.now());
        User nullLoginRequest
                = new User(0, "email", null, "name", LocalDate.now());
        User nullNameRequest
                = new User(0, "email", "login", null, LocalDate.now());

        return Stream.of(nullEmailRequest, nullLoginRequest, nullNameRequest);
    }

    @ParameterizedTest
    @MethodSource("wrongUpdateRequestsStream")
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testIncorrectUserUpdating(User updateUserRequest) {
        String email = "email@mail.ru";
        String login = "login";
        String name = "name";
        LocalDate birthday = LocalDate.now();

        int userIdAdded = createUserInDb(email, login, name, birthday);
        updateUserRequest.setId(userIdAdded);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .as("Проверка обновления пользователя некорректными данными (null где нельзя) %s",
                        updateUserRequest)
                .isThrownBy(() -> userStorage.update(updateUserRequest))
                .withMessageContaining("NULL not allowed for column");
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingUserThatDoesNotExist() {
        String email = "email@mail.ru";
        String login = "login";
        String name = "name";
        LocalDate birthday = LocalDate.now();

        int userIdAdded = createUserInDb(email, login, name, birthday);

        assertThatExceptionOfType(EmptyResultDataAccessException.class)
                .as("Проверка попытки получить несуществующего пользователя")
                .isThrownBy(() -> userStorage.getUser(userIdAdded + 1))
                .withMessage("Incorrect result size: expected 1, actual 0");
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testExistingFriendAddingAndGetting() {
        String email1 = "email1@mail.ru";
        String login1 = "login1";
        String name1 = "name1";
        LocalDate birthday1 = LocalDate.now();

        String email2 = "email2@mail.ru";
        String login2 = "login2";
        String name2 = "name2";
        LocalDate birthday2 = LocalDate.now();

        assertThatCode(() -> {
            int user1IdAdded = createUserInDb(email1, login1, name1, birthday1);
            int user2IdAdded = createUserInDb(email2, login2, name2, birthday2);

            Collection<User> user1Friends = userStorage.getFriends(user1IdAdded);
            assertThat(user1Friends)
                    .as("Проверка изначального отсутствия друзей у пользоавтеля")
                    .isNotNull()
                    .asList()
                    .isEmpty();

            userStorage.addFriend(user1IdAdded, user2IdAdded, Status.CONFIRMED);
            user1Friends = userStorage.getFriends(user1IdAdded);
            assertThat(user1Friends)
                    .as("Проверка добавления в друзья существующего пользователя")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(new User(user2IdAdded, email2, login2, name2, birthday2));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testDuplicatedFriendAdding() {
        String email1 = "email1@mail.ru";
        String login1 = "login1";
        String name1 = "name1";
        LocalDate birthday1 = LocalDate.now();

        String email2 = "email2@mail.ru";
        String login2 = "login2";
        String name2 = "name2";
        LocalDate birthday2 = LocalDate.now();

        assertThatCode(() -> {
            int user1IdAdded = createUserInDb(email1, login1, name1, birthday1);
            int user2IdAdded = createUserInDb(email2, login2, name2, birthday2);

            userStorage.addFriend(user1IdAdded, user2IdAdded, Status.CONFIRMED);
            Collection<User> user1Friends = userStorage.getFriends(user1IdAdded);

            assertThat(user1Friends)
                    .as("Проверка первого добавления пользователя в друзья")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(new User(user2IdAdded, email2, login2, name2, birthday2));

            userStorage.addFriend(user1IdAdded, user2IdAdded, Status.CONFIRMED);
            user1Friends = userStorage.getFriends(user1IdAdded);

            assertThat(user1Friends)
                    .as("Проверка отсутствия повторного добавления пользователя в друзья")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(new User(user2IdAdded, email2, login2, name2, birthday2));
        }).doesNotThrowAnyException();

    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testNotExistingFriendAdding() {
        String email = "email@mail.ru";
        String login = "login";
        String name = "name";
        LocalDate birthday = LocalDate.now();

        int userIdAdded = createUserInDb(email, login, name, birthday);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .as("Проверка добавления в друзья несуществующего пользователя")
                .isThrownBy(() -> userStorage.addFriend(userIdAdded, userIdAdded + 1, Status.CONFIRMED))
                .withMessageContaining("Referential integrity constraint violation");
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testExistingFriendDeletion() {
        String email1 = "email1@mail.ru";
        String login1 = "login1";
        String name1 = "name1";
        LocalDate birthday1 = LocalDate.now();

        String email2 = "email2@mail.ru";
        String login2 = "login2";
        String name2 = "name2";
        LocalDate birthday2 = LocalDate.now();

        assertThatCode(() -> {
            int user1IdAdded = createUserInDb(email1, login1, name1, birthday1);
            int user2IdAdded = createUserInDb(email2, login2, name2, birthday2);
            userStorage.addFriend(user1IdAdded, user2IdAdded, Status.CONFIRMED);
            userStorage.deleteFriend(user1IdAdded, user2IdAdded);

            Collection<User> user1Friends = userStorage.getFriends(user1IdAdded);

            assertThat(user1Friends)
                    .as("Проверка удаления из друзей пользователя, который был в друзьях")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testNotExistingFriendDeletion() {
        String email = "email@mail.ru";
        String login = "login";
        String name = "name";
        LocalDate birthday = LocalDate.now();

        assertThatCode(() -> {
            int userIdAdded = createUserInDb(email, login, name, birthday);
            userStorage.deleteFriend(userIdAdded, userIdAdded + 1);

            Collection<User> user1Friends = userStorage.getFriends(userIdAdded);

            assertThat(user1Friends)
                    .as("Проверка удаления из друзей несуществующего пользователя")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testFriendsGettingOfUserThatDoesNotExist() {
        String email = "email@mail.ru";
        String login = "login";
        String name = "name";
        LocalDate birthday = LocalDate.now();

        assertThatCode(() -> {
            int userIdAdded = createUserInDb(email, login, name, birthday);

            Collection<User> notExistingUserFriends = userStorage.getFriends(userIdAdded + 1);

            assertThat(notExistingUserFriends)
                    .as("Проверка получения списка друзей несуществующего пользователя")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingNotEmptyCommonFriendsList() {
        String email1 = "email1@mail.ru";
        String login1 = "login1";
        String name1 = "name1";
        LocalDate birthday1 = LocalDate.now();

        String email2 = "email2@mail.ru";
        String login2 = "login2";
        String name2 = "name2";
        LocalDate birthday2 = LocalDate.now();

        String email3 = "email3@mail.ru";
        String login3 = "login3";
        String name3 = "name3";
        LocalDate birthday3 = LocalDate.now();

        assertThatCode(() -> {
            int user1IdAdded = createUserInDb(email1, login1, name1, birthday1);
            int user2IdAdded = createUserInDb(email2, login2, name2, birthday2);
            int user3IdAdded = createUserInDb(email3, login3, name3, birthday3);

            userStorage.addFriend(user1IdAdded, user3IdAdded, Status.CONFIRMED);
            userStorage.addFriend(user2IdAdded, user3IdAdded, Status.CONFIRMED);

            Collection<User> commonFriends = userStorage.getCommonFriends(user1IdAdded, user2IdAdded);

            assertThat(commonFriends)
                    .as("Проверка получения непустого списка общих друзей существующих пользователей")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(new User(user3IdAdded, email3, login3, name3, birthday3));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingEmptyCommonFriendsList() {
        String email1 = "email1@mail.ru";
        String login1 = "login1";
        String name1 = "name1";
        LocalDate birthday1 = LocalDate.now();

        String email2 = "email2@mail.ru";
        String login2 = "login2";
        String name2 = "name2";
        LocalDate birthday2 = LocalDate.now();

        assertThatCode(() -> {
            int user1IdAdded = createUserInDb(email1, login1, name1, birthday1);
            int user2IdAdded = createUserInDb(email2, login2, name2, birthday2);

            userStorage.addFriend(user1IdAdded, user2IdAdded, Status.CONFIRMED);


            Collection<User> commonFriends = userStorage.getCommonFriends(user1IdAdded, user2IdAdded);

            assertThat(commonFriends)
                    .as("Проверка получения пустого списка общих друзей существующих пользователей")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingCommonFriendsListOfNotExistingUsers() {
        String email = "email@mail.ru";
        String login = "login";
        String name = "name";
        LocalDate birthday = LocalDate.now();

        assertThatCode(() -> {
            int userIdAdded = createUserInDb(email, login, name, birthday);

            Collection<User> commonFriends = userStorage.getCommonFriends(userIdAdded + 1, userIdAdded + 2);

            assertThat(commonFriends)
                    .as("Проверка получения пустого списка общих друзей несуществующих пользователей")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    private int createFilmInDb(String name, String description, LocalDate releaseDate,
                               int duration, Mpa mpa, Set<Genre> genres) {
        Film addFilmRequest = new Film(
                name,
                description,
                releaseDate,
                duration,
                mpa
        );

        addFilmRequest.setGenres(new HashSet<>(genres));

        return filmStorage.add(addFilmRequest);
    }

    @Test
    void testGettingAllFilmsOfEmptyDb() {
        assertThatCode(() -> {
            Collection<Film> allFilmsFromDb = filmStorage.getAll();

            assertThat(allFilmsFromDb)
                    .as("Проверка получения пустого списка фильмов при пустой бд")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingAllFilms() {
        String name1 = "name1";
        String description1 = "description1";
        LocalDate releaseDate1 = LocalDate.now();
        int duration1 = 120;
        Mpa mpa1 = new Mpa(1, "G");
        Set<Genre> genres1 = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        String name2 = "name2";
        String description2 = "description2";
        LocalDate releaseDate2 = LocalDate.now();
        int duration2 = 32;
        Mpa mpa2 = new Mpa(1, "G");
        Set<Genre> genres2 = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        assertThatCode(() -> {
            int film1IdAdded = createFilmInDb(name1, description1, releaseDate1, duration1, mpa1, genres1);
            int film2IdAdded = createFilmInDb(name2, description2, releaseDate2, duration2, mpa2, genres2);

            Collection<Film> allFilmsFromDb = filmStorage.getAll();

            Film expectedFilm1 = new Film(film1IdAdded, name1, description1, releaseDate1, duration1, mpa1);
            expectedFilm1.setGenres(genres1);

            Film expectedFilm2 = new Film(film2IdAdded, name2, description2, releaseDate2, duration2, mpa2);
            expectedFilm2.setGenres(genres2);

            assertThat(allFilmsFromDb)
                    .as("Проверка получения непустого списка фильмов при непустой бд")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(expectedFilm1)
                    .contains(expectedFilm2);
        }).doesNotThrowAnyException();
    }

    @Test
    void testGettingFilmExistenceWhenFilmDoesNotExist() {
        assertThatCode(() -> {
            int id = 1;
            boolean isFilmExist = filmStorage.isFilmExists(id);

            assertThat(isFilmExist)
                    .as("Проверка получения false при отсутсвии фильма в бд")
                    .isFalse();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingFilmExistenceWhenFilmExists() {
        String name = "name";
        String description = "description";
        LocalDate releaseDate = LocalDate.now();
        int duration = 120;
        Mpa mpa = new Mpa(1, "G");
        Set<Genre> genres = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        assertThatCode(() -> {
            int filmIdAdded = createFilmInDb(name, description, releaseDate, duration, mpa, genres);
            boolean isFilmExist = filmStorage.isFilmExists(filmIdAdded);

            assertThat(isFilmExist)
                    .as("Проверка получения true, когда фильм с таким id есть в бд")
                    .isTrue();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingUserLikeExistenceInFilmWhenLikeDoesNotExist() {
        String name = "name";
        String description = "description";
        LocalDate releaseDate = LocalDate.now();
        int duration = 120;
        Mpa mpa = new Mpa(1, "G");
        Set<Genre> genres = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        int userId = 1;

        assertThatCode(() -> {
            int filmIdAdded = createFilmInDb(name, description, releaseDate, duration, mpa, genres);
            boolean isLikeExists = filmStorage.isFilmContainsUserLike(filmIdAdded, userId);

            assertThat(isLikeExists)
                    .as("Проверка получения false, когда у фильма с таким id нет лайка от пользователя")
                    .isFalse();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingUserLikeExistenceInFilmWhenLikeExists() {
        String name = "name";
        String description = "description";
        LocalDate releaseDate = LocalDate.now();
        int duration = 120;
        Mpa mpa = new Mpa(1, "G");
        Set<Genre> genres = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        String email = "email@mail.ru";
        String login = "login";
        String userName = "name";
        LocalDate birthday = LocalDate.now();

        assertThatCode(() -> {
            int filmIdAdded = createFilmInDb(name, description, releaseDate, duration, mpa, genres);
            int userIdAdded = createUserInDb(email, login, userName, birthday);
            filmStorage.addLike(filmIdAdded, userIdAdded);
            boolean isLikeExists = filmStorage.isFilmContainsUserLike(filmIdAdded, userIdAdded);

            assertThat(isLikeExists)
                    .as("Проверка получения true, когда у фильма с таким id есть лайка от пользователя")
                    .isTrue();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testAddingAndGettingFilmCorrect() {
        String name = "name";
        String description = "description";
        LocalDate releaseDate = LocalDate.now();
        int duration = 120;
        Mpa mpa = new Mpa(1, "G");
        Set<Genre> genres = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        assertThatCode(() -> {
            int filmId = (createFilmInDb(name, description, releaseDate, duration, mpa, genres));

            assertThat(filmId)
                    .as("Проверка получения id добавленного фильма в бд")
                    .isPositive();

            Film filmFromDb = filmStorage.getFilm(filmId);

            assertThat(filmFromDb)
                    .as("Проверка корректности получения фильма из бд")
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("id", filmId)
                    .hasFieldOrPropertyWithValue("name", name)
                    .hasFieldOrPropertyWithValue("description", description)
                    .hasFieldOrPropertyWithValue("releaseDate", releaseDate)
                    .hasFieldOrPropertyWithValue("duration", duration)
                    .hasFieldOrPropertyWithValue("mpa", mpa);

            assertThat(filmFromDb.getGenres())
                    .isEqualTo(genres);
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testCorrectFilmUpdating() {
        String oldName = "oldName";
        String oldDescription = "oldDescription";
        LocalDate oldReleaseDate = LocalDate.now();
        int oldDuration = 60;
        Mpa oldMpa = new Mpa(1, "G");
        Set<Genre> oldGenres = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        int filmId = createFilmInDb(oldName, oldDescription, oldReleaseDate, oldDuration, oldMpa, oldGenres);

        String newName = "newName";
        String newDescription = "newDescription";
        LocalDate newReleaseDate = LocalDate.now();
        int newDuration = 120;
        Mpa newMpa = new Mpa(2, "PG");
        Set<Genre> newGenres = Set.of(new Genre(2, "Драма"));

        Film film = filmStorage.getFilm(filmId);

        assertThat(film)
                .as("Проверка корректности заполнения данными фильма")
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", filmId)
                .hasFieldOrPropertyWithValue("name", oldName)
                .hasFieldOrPropertyWithValue("description", oldDescription)
                .hasFieldOrPropertyWithValue("releaseDate", oldReleaseDate)
                .hasFieldOrPropertyWithValue("duration", oldDuration)
                .hasFieldOrPropertyWithValue("mpa", oldMpa);
        assertThat(film.getGenres())
                .isEqualTo(oldGenres);

        Film updateUserRequest = new Film(filmId, newName, newDescription, newReleaseDate, newDuration, newMpa);
        updateUserRequest.setGenres(newGenres);


        assertThatCode(() -> filmStorage.update(updateUserRequest))
                .doesNotThrowAnyException();

        film = filmStorage.getFilm(filmId);

        assertThat(film)
                .as("Проверка корректности обновления данных")
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", filmId)
                .hasFieldOrPropertyWithValue("name", newName)
                .hasFieldOrPropertyWithValue("description", newDescription)
                .hasFieldOrPropertyWithValue("releaseDate", newReleaseDate)
                .hasFieldOrPropertyWithValue("duration", newDuration)
                .hasFieldOrPropertyWithValue("mpa", newMpa);
        assertThat(film.getGenres())
                .isEqualTo(newGenres);
    }

    static Stream<Film> wrongFilmUpdateRequestsStream() {
        Mpa mpa = new Mpa(1, "G");

        Film fullNullRequest = new Film(-1, null, null, null, -1, mpa);
        Film null1Request = new Film(-1, null, "description", LocalDate.now(), 30, mpa);
        Film null2Request = new Film(-1, "name", "description", LocalDate.now(), 30,
                new Mpa(0, null));
        Film incorrectDurationRequest = new Film(-1, "name", "description", LocalDate.now(),
                -30, mpa);

        return Stream.of(fullNullRequest, null1Request, null2Request, incorrectDurationRequest);
    }

    @ParameterizedTest
    @MethodSource("wrongFilmUpdateRequestsStream")
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testIncorrectFilmUpdating(Film updateFilmRequest) {
        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .as("Проверка обновления фильма некорректными данными %s", updateFilmRequest)
                .isThrownBy(() -> {
                    int filmId = createFilmInDb(
                            updateFilmRequest.getName(),
                            updateFilmRequest.getDescription(),
                            updateFilmRequest.getReleaseDate(),
                            updateFilmRequest.getDuration(),
                            updateFilmRequest.getMpa(),
                            new LinkedHashSet<>(updateFilmRequest.getGenres()));

                    updateFilmRequest.setId(filmId);

                    filmStorage.update(updateFilmRequest);
                });
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingFilmThatDoesNotExist() {
        String name = "name";
        String description = "description";
        LocalDate releaseDate = LocalDate.now();
        int duration = 120;
        Mpa mpa = new Mpa(1, "G");
        Set<Genre> genres = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        int filmId = createFilmInDb(name, description, releaseDate, duration, mpa, genres);

        assertThatExceptionOfType(EmptyResultDataAccessException.class)
                .as("Проверка попытки получить несуществующий фильм")
                .isThrownBy(() -> userStorage.getUser(filmId + 1))
                .withMessage("Incorrect result size: expected 1, actual 0");
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testCorrectLikeAddition() {
        String name = "name";
        String description = "description";
        LocalDate releaseDate = LocalDate.now();
        int duration = 120;
        Mpa mpa = new Mpa(1, "G");
        Set<Genre> genres = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        int filmId = createFilmInDb(name, description, releaseDate, duration, mpa, genres);

        String email = "email@mail.ru";
        String login = "login";
        String userName = "name";
        LocalDate birthday = LocalDate.now();

        int userId = createUserInDb(email, login, userName, birthday);

        assertThatCode(() -> filmStorage.addLike(filmId, userId)).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testIncorrectLikeAdditionWhenFilmDoesNotExist() {
        String email = "email@mail.ru";
        String login = "login";
        String name = "name";
        LocalDate birthday = LocalDate.now();

        int userIdAdded = createUserInDb(email, login, name, birthday);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .as("Проверка добавления лайка несуществующему фильму")
                .isThrownBy(() -> filmStorage.addLike(1, userIdAdded))
                .withMessageContaining("Referential integrity constraint violation");
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testIncorrectLikeAdditionWhenUserDoesNotExist() {
        String name = "name";
        String description = "description";
        LocalDate releaseDate = LocalDate.now();
        int duration = 120;
        Mpa mpa = new Mpa(1, "G");
        Set<Genre> genres = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        int filmId = createFilmInDb(name, description, releaseDate, duration, mpa, genres);

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .as("Проверка добавления лайка несуществующим пользователем")
                .isThrownBy(() -> filmStorage.addLike(filmId, 1))
                .withMessageContaining("Referential integrity constraint violation");
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testExistingLikeDeletion() {
        String name = "name";
        String description = "description";
        LocalDate releaseDate = LocalDate.now();
        int duration = 120;
        Mpa mpa = new Mpa(1, "G");
        Set<Genre> genres = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        int filmId = createFilmInDb(name, description, releaseDate, duration, mpa, genres);

        String email = "email@mail.ru";
        String login = "login";
        String userName = "name";
        LocalDate birthday = LocalDate.now();

        int userId = createUserInDb(email, login, userName, birthday);

        assertThatCode(() -> {
            filmStorage.addLike(filmId, userId);
            filmStorage.deleteLike(filmId, userId);
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testNotExistingLikeDeletion() {
        String name = "name";
        String description = "description";
        LocalDate releaseDate = LocalDate.now();
        int duration = 120;
        Mpa mpa = new Mpa(1, "G");
        Set<Genre> genres = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        int filmId = createFilmInDb(name, description, releaseDate, duration, mpa, genres);

        String email = "email@mail.ru";
        String login = "login";
        String userName = "name";
        LocalDate birthday = LocalDate.now();

        int userId = createUserInDb(email, login, userName, birthday);

        assertThatCode(() -> {
            filmStorage.addLike(filmId, userId);
            filmStorage.deleteLike(filmId, 100);
            filmStorage.deleteLike(100, userId);
        }).doesNotThrowAnyException();
    }

    @Test
    void testGettingBestFilmsOfEmptyDb() {
        assertThatCode(() -> {
            Collection<Film> allFilmsFromDb = filmStorage.getBestFilms(10);

            assertThat(allFilmsFromDb)
                    .as("Проверка получения пустого списка лучших фильмов при пустой бд")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingBestFilmsWhenLikesExist() {
        String name1 = "nameA";
        String description1 = "description1";
        LocalDate releaseDate1 = LocalDate.now();
        int duration1 = 120;
        Mpa mpa1 = new Mpa(1, "G");
        Set<Genre> genres1 = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        String name2 = "nameC";
        String description2 = "description2";
        LocalDate releaseDate2 = LocalDate.now();
        int duration2 = 32;
        Mpa mpa2 = new Mpa(1, "G");
        Set<Genre> genres2 = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));


        String email = "email@mail.ru";
        String login = "login";
        String userName = "name";
        LocalDate birthday = LocalDate.now();


        assertThatCode(() -> {
            int film1IdAdded = createFilmInDb(name1, description1, releaseDate1, duration1, mpa1, genres1);
            int film2IdAdded = createFilmInDb(name2, description2, releaseDate2, duration2, mpa2, genres2);
            int userId = createUserInDb(email, login, userName, birthday);

            filmStorage.addLike(film2IdAdded, userId);

            Collection<Film> allFilmsFromDb = filmStorage.getBestFilms(10);

            Film expectedFilm1 = new Film(film1IdAdded, name1, description1, releaseDate1, duration1, mpa1);
            expectedFilm1.setGenres(genres1);

            Film expectedFilm2 = new Film(film2IdAdded, name2, description2, releaseDate2, duration2, mpa2);
            expectedFilm2.setGenres(genres2);
            expectedFilm2.addLike(userId);

            assertThat(allFilmsFromDb)
                    .as("Проверка получения непустого списка лучших фильмов отсортированного по количеству" +
                            " лайков")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(expectedFilm1, Index.atIndex(1))
                    .contains(expectedFilm2, Index.atIndex(0));

            filmStorage.deleteLike(film2IdAdded, userId);
            expectedFilm2.deleteLike(userId);

            allFilmsFromDb = filmStorage.getBestFilms(10);

            assertThat(allFilmsFromDb)
                    .as("Проверка получения непустого списка лучших фильмов без лайков," +
                            " отсортированного по алфавиту")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(expectedFilm1, Index.atIndex(0))
                    .contains(expectedFilm2, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingBestFilmsWhenLikesDoNotExist() {
        String name1 = "nameA";
        String description1 = "description1";
        LocalDate releaseDate1 = LocalDate.now();
        int duration1 = 120;
        Mpa mpa1 = new Mpa(1, "G");
        Set<Genre> genres1 = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        String name2 = "nameC";
        String description2 = "description2";
        LocalDate releaseDate2 = LocalDate.now();
        int duration2 = 32;
        Mpa mpa2 = new Mpa(1, "G");
        Set<Genre> genres2 = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        assertThatCode(() -> {
            int film1IdAdded = createFilmInDb(name1, description1, releaseDate1, duration1, mpa1, genres1);
            int film2IdAdded = createFilmInDb(name2, description2, releaseDate2, duration2, mpa2, genres2);

            Collection<Film> allFilmsFromDb = filmStorage.getBestFilms(10);

            Film expectedFilm1 = new Film(film1IdAdded, name1, description1, releaseDate1, duration1, mpa1);
            expectedFilm1.setGenres(genres1);

            Film expectedFilm2 = new Film(film2IdAdded, name2, description2, releaseDate2, duration2, mpa2);
            expectedFilm2.setGenres(genres2);

            assertThat(allFilmsFromDb)
                    .as("Проверка получения непустого списка лучших фильмов без лайков," +
                            " отсортированного по алфавиту")
                    .isNotNull()
                    .asList()
                    .hasSize(2)
                    .contains(expectedFilm1, Index.atIndex(0))
                    .contains(expectedFilm2, Index.atIndex(1));
        }).doesNotThrowAnyException();
    }

    @Test
    void testGettingAllGenres() {
        assertThatCode(() -> {
            Collection<Genre> genres = genreStorage.getAll();

            assertThat(genres)
                    .as("Проверка получения корректного списка жанров")
                    .isNotNull()
                    .asList()
                    .hasSize(6)
                    .contains(new Genre(1, "Комедия"))
                    .contains(new Genre(2, "Драма"))
                    .contains(new Genre(3, "Мультфильм"))
                    .contains(new Genre(4, "Триллер"))
                    .contains(new Genre(5, "Документальный"))
                    .contains(new Genre(6, "Боевик"));
        }).doesNotThrowAnyException();
    }

    @Test
    void testGettingGenreByExistingId() {
        assertThatCode(() -> {
            Genre genre = genreStorage.getById(1);

            assertThat(genre)
                    .as("Проверка получения существующего жанра по id")
                    .isNotNull()
                    .isEqualTo(new Genre(1, "Комедия"));
        }).doesNotThrowAnyException();
    }

    @Test
    void testGettingGenreByNotExistingId() {
        assertThatExceptionOfType(EmptyResultDataAccessException.class)
                .as("Проверка получения несуществующего жанра")
                .isThrownBy(() -> genreStorage.getById(10))
                .withMessageContaining("Incorrect result size: expected 1, actual 0");

    }

    @Test
    void testGettingGenreExistenceWhenGenreExists() {
        assertThatCode(() -> {
            boolean isGenreExist = genreStorage.isGenreExists(1);

            assertThat(isGenreExist)
                    .as("Проверка получения true, когда жанр с таким id есть в бд")
                    .isTrue();
        }).doesNotThrowAnyException();
    }

    @Test
    void testGettingGenreExistenceWhenGenreDoesNotExist() {
        assertThatCode(() -> {
            boolean isGenreExist = genreStorage.isGenreExists(10);

            assertThat(isGenreExist)
                    .as("Проверка получения false при отсутсвии жанра в бд")
                    .isFalse();
        }).doesNotThrowAnyException();
    }

    @Test
    void testGettingAllRatings() {
        assertThatCode(() -> {
            Collection<Mpa> mpas = mpaStorage.getAll();

            assertThat(mpas)
                    .as("Проверка получения корректного списка рейтингов")
                    .isNotNull()
                    .asList()
                    .hasSize(5)
                    .contains(new Mpa(1, "G"))
                    .contains(new Mpa(2, "PG"))
                    .contains(new Mpa(3, "PG-13"))
                    .contains(new Mpa(4, "R"))
                    .contains(new Mpa(5, "NC-17"));
        }).doesNotThrowAnyException();
    }

    @Test
    void testGettingRatingByExistingId() {
        assertThatCode(() -> {
            Mpa mpa = mpaStorage.getById(1);

            assertThat(mpa)
                    .as("Проверка получения существующего рейтинга по id")
                    .isNotNull()
                    .isEqualTo(new Mpa(1, "G"));
        }).doesNotThrowAnyException();
    }

    @Test
    void testGettingRatingByNotExistingId() {
        assertThatExceptionOfType(EmptyResultDataAccessException.class)
                .as("Проверка получения несуществующего рейтинга")
                .isThrownBy(() -> genreStorage.getById(10))
                .withMessageContaining("Incorrect result size: expected 1, actual 0");

    }

    @Test
    void testGettingRatingExistenceWhenRatingExists() {
        assertThatCode(() -> {
            boolean isRatingExist = mpaStorage.isRatingExists(1);

            assertThat(isRatingExist)
                    .as("Проверка получения true, когда рейтинг с таким id есть в бд")
                    .isTrue();
        }).doesNotThrowAnyException();
    }

    @Test
    void testGettingRatingExistenceWhenRatingDoesNotExist() {
        assertThatCode(() -> {
            boolean isGenreExist = genreStorage.isGenreExists(10);

            assertThat(isGenreExist)
                    .as("Проверка получения false при отсутсвии рейтинга в бд")
                    .isFalse();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingNotEmptyCommonFilmsList() {
        String email1 = "email1@mail.ru";
        String login1 = "login1";
        String name1 = "name1";
        LocalDate birthday1 = LocalDate.now();

        String email2 = "email2@mail.ru";
        String login2 = "login2";
        String name2 = "name2";
        LocalDate birthday2 = LocalDate.now();

        String name = "name";
        String description = "description";
        LocalDate releaseDate = LocalDate.now();
        int duration = 120;
        Mpa mpa = new Mpa(1, "G");
        Set<Genre> genres = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        assertThatCode(() -> {
            int user1IdAdded = createUserInDb(email1, login1, name1, birthday1);
            int user2IdAdded = createUserInDb(email2, login2, name2, birthday2);

            int filmId = createFilmInDb(name, description, releaseDate, duration, mpa, genres);

            filmStorage.addLike(filmId, user1IdAdded);
            filmStorage.addLike(filmId, user2IdAdded);

            Collection<Film> commonFilms = filmStorage.getCommonFilms(user1IdAdded, user2IdAdded);

            Film expectedFilm = new Film(filmId, name, description, releaseDate, duration, mpa);
            expectedFilm.setGenres(genres);
            expectedFilm.addLike(user1IdAdded);
            expectedFilm.addLike(user2IdAdded);

            assertThat(commonFilms)
                    .as("Проверка получения непустого списка общих любимых фильмов существующих пользователей")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(expectedFilm);
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingEmptyCommonFilmsList() {
        String email1 = "email1@mail.ru";
        String login1 = "login1";
        String name1 = "name1";
        LocalDate birthday1 = LocalDate.now();

        String email2 = "email2@mail.ru";
        String login2 = "login2";
        String name2 = "name2";
        LocalDate birthday2 = LocalDate.now();

        assertThatCode(() -> {
            int user1IdAdded = createUserInDb(email1, login1, name1, birthday1);
            int user2IdAdded = createUserInDb(email2, login2, name2, birthday2);

            Collection<Film> commonFilms = filmStorage.getCommonFilms(user1IdAdded, user2IdAdded);

            assertThat(commonFilms)
                    .as("Проверка получения пустого списка общих любимых фильмов существующих пользователей")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingCommonFilmsListOfNotExistingUsers() {
        String email = "email@mail.ru";
        String login = "login";
        String name = "name";
        LocalDate birthday = LocalDate.now();

        assertThatCode(() -> {
            int userIdAdded = createUserInDb(email, login, name, birthday);

            Collection<Film> commonFilms = filmStorage.getCommonFilms(userIdAdded + 1, userIdAdded + 2);

            assertThat(commonFilms)
                    .as("Проверка получения пустого списка общих любимых фильмов несуществующих пользователей")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingSimilarInterestsForNotExistingUsers() {
        assertThatCode(() -> {
            Collection<Film> recommendations = filmStorage.getRecommendations(1);

            assertThat(recommendations)
                    .as("Проверка получения списка рекомендуемых фильмов для пользователя с несуществующим id")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingSimilarInterestsForUserWithNoLikes() {
        String email1 = "email1@mail.ru";
        String login1 = "login1";
        String name1 = "name1";
        LocalDate birthday1 = LocalDate.now();

        String email2 = "email2@mail.ru";
        String login2 = "login2";
        String name2 = "name2";
        LocalDate birthday2 = LocalDate.now();

        String filmName1 = "Film1";
        String description1 = "description1";
        LocalDate releaseDate1 = LocalDate.now();
        int duration1 = 120;
        Mpa mpa1 = new Mpa(1, "G");
        Set<Genre> genres1 = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        String filmName2 = "Film2";
        String description2 = "description2";
        LocalDate releaseDate2 = LocalDate.now();
        int duration2 = 180;
        Mpa mpa2 = new Mpa(2, "PG");
        Set<Genre> genres2 = new LinkedHashSet<>(List.of(new Genre(4, "Триллер")));

        assertThatCode(() -> {
            createFilmInDb(filmName1, description1, releaseDate1, duration1, mpa1, genres1);
            createFilmInDb(filmName2, description2, releaseDate2, duration2, mpa2, genres2);
            int userId = createUserInDb(email1, login1, name1, birthday1);
            createUserInDb(email2, login2, name2, birthday2);

            Collection<Film> recommendations = filmStorage.getRecommendations(userId);

            assertThat(recommendations)
                    .as("Проверка получения списка рекомендуемых фильмов для пользователя с без лайков")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingSimilarInterestsForUserWithNoSimilarLikes() {
        String email1 = "email1@mail.ru";
        String login1 = "login1";
        String name1 = "name1";
        LocalDate birthday1 = LocalDate.now();

        String email2 = "email2@mail.ru";
        String login2 = "login2";
        String name2 = "name2";
        LocalDate birthday2 = LocalDate.now();

        String filmName1 = "Film1";
        String description1 = "description1";
        LocalDate releaseDate1 = LocalDate.now();
        int duration1 = 120;
        Mpa mpa1 = new Mpa(1, "G");
        Set<Genre> genres1 = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        String filmName2 = "Film2";
        String description2 = "description2";
        LocalDate releaseDate2 = LocalDate.now();
        int duration2 = 180;
        Mpa mpa2 = new Mpa(2, "PG");
        Set<Genre> genres2 = new LinkedHashSet<>(List.of(new Genre(4, "Триллер")));

        assertThatCode(() -> {
            int filmId1 = createFilmInDb(filmName1, description1, releaseDate1, duration1, mpa1, genres1);
            int filmId2 = createFilmInDb(filmName2, description2, releaseDate2, duration2, mpa2, genres2);
            int userId1 = createUserInDb(email1, login1, name1, birthday1);
            int userId2 = createUserInDb(email2, login2, name2, birthday2);
            filmStorage.addLike(filmId1, userId1);
            filmStorage.addLike(filmId2, userId2);

            Collection<Film> recommendations = filmStorage.getRecommendations(userId1);

            assertThat(recommendations)
                    .as("Проверка получения списка рекомендуемых фильмов для пользователем без общих лайков")
                    .isNotNull()
                    .asList()
                    .isEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @Sql(scripts = "classpath:db/clearDb.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testGettingSimilarInterestsForUserWithOneSimilarLike() {
        String email1 = "email1@mail.ru";
        String login1 = "login1";
        String name1 = "name1";
        LocalDate birthday1 = LocalDate.now();

        String email2 = "email2@mail.ru";
        String login2 = "login2";
        String name2 = "name2";
        LocalDate birthday2 = LocalDate.now();

        String filmName1 = "Film1";
        String description1 = "description1";
        LocalDate releaseDate1 = LocalDate.now();
        int duration1 = 120;
        Mpa mpa1 = new Mpa(1, "G");
        Set<Genre> genres1 = new LinkedHashSet<>(List.of(new Genre(1, "Комедия")));

        String filmName2 = "Film2";
        String description2 = "description2";
        LocalDate releaseDate2 = LocalDate.now();
        int duration2 = 180;
        Mpa mpa2 = new Mpa(2, "PG");
        Set<Genre> genres2 = new LinkedHashSet<>(List.of(new Genre(4, "Триллер")));

        assertThatCode(() -> {
            int filmId1 = createFilmInDb(filmName1, description1, releaseDate1, duration1, mpa1, genres1);
            int filmId2 = createFilmInDb(filmName2, description2, releaseDate2, duration2, mpa2, genres2);
            int userId1 = createUserInDb(email1, login1, name1, birthday1);
            int userId2 = createUserInDb(email2, login2, name2, birthday2);

            filmStorage.addLike(filmId1, userId1);
            filmStorage.addLike(filmId1, userId2);
            filmStorage.addLike(filmId2, userId2);

            Film expectedFilm = new Film(filmName2, description2, releaseDate2, duration2, mpa2);
            expectedFilm.setGenres(genres2);
            expectedFilm.setId(filmId2);
            expectedFilm.addLike(userId2);

            Collection<Film> recommendations = filmStorage.getRecommendations(userId1);

            assertThat(recommendations)
                    .as("Проверка получения списка рекомендуемых фильмов для пользователя с одним общим лайком")
                    .isNotNull()
                    .asList()
                    .hasSize(1)
                    .contains(expectedFilm);
        }).doesNotThrowAnyException();
    }
}
