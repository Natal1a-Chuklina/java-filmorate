package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.AlreadyExistException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private static final int NOT_EXISTING_ID = 1000;
    private UserService userService;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void beforeEach() {
        userService = new UserService(new InMemoryUserStorage());

        user1 = new User("email@mail.ru", "login1", "name1",
                LocalDate.of(2000, 1, 1));
        user2 = new User("email@mail.ru", "login2", "name2",
                LocalDate.of(2001, 1, 1));
        user3 = new User("email@mail.ru", "login3", "name3",
                LocalDate.of(2002, 1, 1));
    }

    @Test
    void testUserCreation() {
        assertEquals(1, userService.createUser(user1).getId());
        assertEquals(2, userService.createUser(user2).getId());
        assertEquals(2, userService.getAll().size());
    }

    @Test
    void testUserCreationWithExistingId() {
        user2.setId(1);

        assertEquals(1, userService.createUser(user1).getId());
        assertThrows(AlreadyExistException.class, () -> userService.createUser(user2));
        assertEquals(1, userService.getAll().size());
    }

    @Test
    void testGettingAllUsersWhenUsersNotExist() {
        assertTrue(userService.getAll().isEmpty());
    }

    @Test
    void testGettingAllUsers() {
        userService.createUser(user1);
        userService.createUser(user2);
        userService.createUser(user3);

        assertEquals(List.of(user1, user2, user3), new ArrayList<>(userService.getAll()));
    }

    @Test
    void testUserUpdating() {
        userService.createUser(user1);
        user2.setId(user1.getId());

        assertEquals(user2, userService.updateUser(user2));
    }

    @Test
    void testNotExistingUserUpdating() {
        user2.setId(NOT_EXISTING_ID);

        assertThrows(NotFoundException.class, () -> userService.updateUser(user2));
    }

    @Test
    void testUserUpdatingWithNullIdField() {
        assertThrows(NotFoundException.class, () -> userService.updateUser(user2));
    }

    @Test
    void testGettingUserByExistingId() {
        userService.createUser(user1);

        assertEquals(user1, userService.getUserById(user1.getId()));
    }

    @Test
    void testGettingUserByNotExistingId() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(NOT_EXISTING_ID));
    }

    @Test
    void testFriendAddition() {
        userService.createUser(user1);
        userService.createUser(user2);

        userService.addFriend(user1.getId(), user2.getId());

        assertEquals(1, user1.getFriends().size());
        assertEquals(1, user2.getFriends().size());

        assertTrue(user1.getFriends().contains(user2.getId()));
        assertTrue(user2.getFriends().contains(user1.getId()));
    }

    @Test
    void testFriendAdditionWhenOneOfUsersDoesNotExist() {
        userService.createUser(user1);

        assertThrows(NotFoundException.class, () -> userService.addFriend(NOT_EXISTING_ID, user1.getId()));
        assertThrows(NotFoundException.class, () -> userService.addFriend(user1.getId(), NOT_EXISTING_ID));
    }

    @Test
    void testRepeatFriendAddition() {
        userService.createUser(user1);
        userService.createUser(user2);

        userService.addFriend(user1.getId(), user2.getId());

        assertThrows(AlreadyExistException.class, () -> userService.addFriend(user1.getId(), user2.getId()));
        assertThrows(AlreadyExistException.class, () -> userService.addFriend(user2.getId(), user1.getId()));
    }

    @Test
    void testItselfUserFriendAddition() {
        userService.createUser(user1);

        assertThrows(IllegalArgumentException.class, () -> userService.addFriend(user1.getId(), user1.getId()));
    }

    @Test
    void testFriendDeletion() {
        userService.createUser(user1);
        userService.createUser(user2);

        userService.addFriend(user1.getId(), user2.getId());
        assertEquals(1, user1.getFriends().size());
        assertEquals(1, user2.getFriends().size());

        userService.deleteFriend(user2.getId(), user1.getId());

        assertEquals(0, user1.getFriends().size());
        assertEquals(0, user2.getFriends().size());
    }

    @Test
    void testFriendDeletionWhenOneOfUsersDoesNotExist() {
        userService.createUser(user1);

        assertThrows(NotFoundException.class, () -> userService.deleteFriend(NOT_EXISTING_ID, user1.getId()));
        assertThrows(NotFoundException.class, () -> userService.deleteFriend(user1.getId(), NOT_EXISTING_ID));
    }

    @Test
    void testNotExistingFriendDeletion() {
        userService.createUser(user1);
        userService.createUser(user2);

        assertThrows(AlreadyExistException.class, () -> userService.deleteFriend(user1.getId(), user2.getId()));
        assertThrows(AlreadyExistException.class, () -> userService.deleteFriend(user2.getId(), user1.getId()));
    }

    @Test
    void testGettingFriendsListByNotExistingId() {
        assertThrows(NotFoundException.class, () -> userService.getFriendsList(NOT_EXISTING_ID));
    }

    @Test
    void testGettingEmptyFriendsList() {
        userService.createUser(user1);

        assertTrue(userService.getFriendsList(user1.getId()).isEmpty());
    }

    @Test
    void testGettingNotEmptyFriendsList() {
        userService.createUser(user1);
        userService.createUser(user2);
        userService.createUser(user3);

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user3.getId(), user1.getId());

        assertEquals(List.of(user2, user3), userService.getFriendsList(user1.getId()));
    }

    @Test
    void testGettingSameFriendsListWhenOneOfUsersDoesNotExist() {
        userService.createUser(user1);

        assertThrows(NotFoundException.class, () -> userService.getSameFriendsList(NOT_EXISTING_ID, user1.getId()));
        assertThrows(NotFoundException.class, () -> userService.getSameFriendsList(user1.getId(), NOT_EXISTING_ID));
    }

    @Test
    void testGettingEmptySameFriendsList() {
        userService.createUser(user1);
        userService.createUser(user2);
        userService.createUser(user3);

        assertTrue(userService.getSameFriendsList(user1.getId(), user2.getId()).isEmpty());

        userService.addFriend(user1.getId(), user3.getId());
        userService.addFriend(user2.getId(), user3.getId());

        assertTrue(userService.getSameFriendsList(user1.getId(), user3.getId()).isEmpty());
    }

    @Test
    void testGettingNotEmptySameFriendsList() {
        userService.createUser(user1);
        userService.createUser(user2);
        userService.createUser(user3);

        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());
        userService.addFriend(user2.getId(), user3.getId());

        assertEquals(List.of(user3), userService.getSameFriendsList(user1.getId(), user2.getId()));
    }

}