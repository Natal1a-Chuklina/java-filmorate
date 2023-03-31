package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class FilmMapper implements RowMapper<Film> {

    private static final int DATA_COLUMN = 2;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        int duration = rs.getInt("duration");
        Date releaseDateRow = rs.getDate("release_date");
        LocalDate releaseDate = (releaseDateRow == null) ? null : releaseDateRow.toLocalDate();
        Mpa mpa = new Mpa(rs.getInt("rating_id"), rs.getString("rating_name"));

        Film film = new Film(id, name, description, releaseDate, duration, mpa);

        ResultSet genresDataResultSet = rs.getArray("genres_data").getResultSet();
        while (genresDataResultSet.next()) {
            String genreData = genresDataResultSet.getString(DATA_COLUMN);

            if (genreData == null) {
                break;
            }

            String[] data = genreData.split(" ");
            int genreId = Integer.parseInt(data[0]);
            String genreName = data[1];

            film.addGenre(new Genre(genreId, genreName));
        }

        genresDataResultSet.close();

        ResultSet likesDataResultSet = rs.getArray("likes_data").getResultSet();
        while (likesDataResultSet.next()) {
            String likesData = likesDataResultSet.getString(DATA_COLUMN);

            if (likesData == null) {
                break;
            }

            int userId = Integer.parseInt(likesData);

            film.addLike(userId);
        }

        likesDataResultSet.close();

        ResultSet directorsDataResultSet = rs.getArray("directors_data").getResultSet();
        while (directorsDataResultSet.next()) {
            String directorData = directorsDataResultSet.getString(DATA_COLUMN);

            if (directorData == null) {
                break;
            }

            String[] data = directorData.split(",");
            int directorId = Integer.parseInt(data[0]);
            String directorName = data[1];

            film.addDirector(new Director(directorId, directorName));
        }

        directorsDataResultSet.close();

        return film;
    }
}
