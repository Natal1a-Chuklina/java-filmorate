package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Status;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class UserMapper implements RowMapper<User> {
    private static final int FRIENDS_DATA_COLUMN = 2;

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("id");
        String email = rs.getString("email");
        String login = rs.getString("login");
        String name = rs.getString("name");
        Date birthdayRow = rs.getDate("birthday");
        LocalDate birthday = (birthdayRow == null) ? null : birthdayRow.toLocalDate();

        User user = new User(id, email, login, name, birthday);

        ResultSet friendsDataResultSet = rs.getArray("friends_data").getResultSet();
        while (friendsDataResultSet.next()) {
            String friendData = friendsDataResultSet.getString(FRIENDS_DATA_COLUMN);

            if (friendData == null) {
                break;
            }

            String[] data = friendData.split(" ");
            int friendId = Integer.parseInt(data[0]);
            Status friendshipStatus = Status.valueOf(data[1].toUpperCase());

            user.addFriend(friendId, friendshipStatus);
        }

        friendsDataResultSet.close();

        return user;
    }
}
