package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {
    private static final int NOT_EXISTING_ID = 1000;
    private FilmService filmService;
    private UserService userService;
    private Film film1;
    private Film film2;
    private User user;

    @BeforeEach
    public void beforeEach() {
        InMemoryUserStorage inMemoryUserStorage = new InMemoryUserStorage();
        userService = new UserService(inMemoryUserStorage);
        filmService = new FilmService(new InMemoryFilmStorage(), inMemoryUserStorage);

        film1 = new Film("film1 name", "description", LocalDate.of(2000, 1, 1),
                120);
        film2 = new Film("film2 name", "description", LocalDate.of(2010, 1, 1),
                120);
        user = new User("email@mail.ru", "login", "name", LocalDate.of(2000, 1, 1));
    }

    @Test
    void testFilmCreation() {
        assertEquals(1, filmService.createFilm(film1).getId());
        assertEquals(2, filmService.createFilm(film2).getId());
        assertEquals(2, filmService.getAll().size());
    }

    @Test
    void testFilmCreationWithExistingId() {
        film2.setId(1);

        assertEquals(1, filmService.createFilm(film1).getId());
        assertThrows(AlreadyExistException.class, () -> filmService.createFilm(film2));
        assertEquals(1, filmService.getAll().size());
    }

    @Test
    void testGettingAllFilmsWhenFilmsNotExist() {
        assertTrue(filmService.getAll().isEmpty());
    }

    @Test
    void testGettingAllFilms() {
        filmService.createFilm(film1);
        filmService.createFilm(film2);

        assertEquals(List.of(film1, film2), new ArrayList<>(filmService.getAll()));
    }

    @Test
    void testFilmUpdating() {
        filmService.createFilm(film1);
        film2.setId(film1.getId());

        assertEquals(film2, filmService.updateFilm(film2));
    }

    @Test
    void testNotExistingFilmUpdating() {
        film2.setId(NOT_EXISTING_ID);

        assertThrows(NotFoundException.class, () -> filmService.updateFilm(film2));
    }

    @Test
    void testFilmUpdatingWithNullIdField() {
        assertThrows(NotFoundException.class, () -> filmService.updateFilm(film2));
    }

    @Test
    void testGettingFilmByExistingId() {
        filmService.createFilm(film1);

        assertEquals(film1, filmService.getFilmById(film1.getId()));
    }

    @Test
    void testGettingFilmByNotExistingId() {
        assertThrows(NotFoundException.class, () -> filmService.getFilmById(NOT_EXISTING_ID));
    }

    @Test
    void testLikeAddition() {
        filmService.createFilm(film1);
        userService.createUser(user);

        assertEquals(0, film1.getLikes().size());

        filmService.addLike(film1.getId(), user.getId());

        assertEquals(1, film1.getLikes().size());
        assertTrue(film1.getLikes().contains(user.getId()));
    }

    @Test
    void testLikeAdditionWhenFilmDoesNotExist() {
        userService.createUser(user);

        assertThrows(NotFoundException.class, () -> filmService.addLike(NOT_EXISTING_ID, user.getId()));
    }

    @Test
    void testLikeAdditionWhenUserDoesNotExist() {
        filmService.createFilm(film1);

        assertThrows(NotFoundException.class, () -> filmService.addLike(film1.getId(), NOT_EXISTING_ID));
    }

    @Test
    void testRepeatLikeAddition() {
        filmService.createFilm(film1);
        userService.createUser(user);

        filmService.addLike(film1.getId(), user.getId());

        assertThrows(AlreadyExistException.class, () -> filmService.addLike(film1.getId(), user.getId()));
    }

    @Test
    void testLikeDeletion() {
        filmService.createFilm(film1);
        userService.createUser(user);

        filmService.addLike(film1.getId(), user.getId());
        assertEquals(1, film1.getLikes().size());

        filmService.deleteLike(film1.getId(), user.getId());
        assertEquals(0, film1.getLikes().size());
    }

    @Test
    void testLikeDeletionWhenFilmDoesNotExist() {
        userService.createUser(user);

        assertThrows(NotFoundException.class, () -> filmService.deleteLike(NOT_EXISTING_ID, user.getId()));
    }

    @Test
    void testLikeDeletionWhenUserDoesNotExist() {
        filmService.createFilm(film1);
        userService.createUser(user);
        filmService.addLike(film1.getId(), user.getId());

        assertThrows(NotFoundException.class, () -> filmService.deleteLike(film1.getId(), NOT_EXISTING_ID));
    }

    @Test
    void testNotExistingLikeDeletion() {
        filmService.createFilm(film1);
        userService.createUser(user);

        assertThrows(AlreadyExistException.class, () -> filmService.deleteLike(film1.getId(), user.getId()));
    }

    @Test
    void testGettingBestFilmsListWhenListIsEmpty() {
        assertTrue(filmService.getBestFilmsList(1).isEmpty());
    }

    @Test
    void testGettingBestFilmsWhenCountMoreThanListSize() {
        Film film3 = new Film("film3 name", "description", LocalDate.of(2020, 1, 1),
                120);
        User user2 = new User("email@mail.ru", "login2", "name2", LocalDate.of(2001, 1,
                1));

        filmService.createFilm(film1);
        filmService.createFilm(film2);
        filmService.createFilm(film3);

        userService.createUser(user);
        userService.createUser(user2);

        filmService.addLike(film3.getId(), user.getId());
        filmService.addLike(film3.getId(), user2.getId());

        filmService.addLike(film2.getId(), user.getId());

        assertEquals(List.of(film3, film2, film1), filmService.getBestFilmsList(5));
    }

    @Test
    void testGettingBestFilmsWhenCountLessThanListSize() {
        Film film3 = new Film("film3 name", "description", LocalDate.of(2020, 1, 1),
                120);
        User user2 = new User("email@mail.ru", "login2", "name2", LocalDate.of(2001, 1,
                1));

        filmService.createFilm(film1);
        filmService.createFilm(film2);
        filmService.createFilm(film3);

        userService.createUser(user);
        userService.createUser(user2);

        filmService.addLike(film3.getId(), user.getId());
        filmService.addLike(film3.getId(), user2.getId());

        filmService.addLike(film2.getId(), user.getId());

        assertEquals(List.of(film3, film2), filmService.getBestFilmsList(2));
    }
}