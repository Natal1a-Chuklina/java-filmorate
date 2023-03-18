package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class FilmMapper implements RowMapper<Film> {

    private static final int GENRES_DATA_COLUMN = 2;

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
            String genreData = genresDataResultSet.getString(GENRES_DATA_COLUMN);

            if (genreData == null) {
                break;
            }

            String[] data = genreData.split(" ");
            int genreId = Integer.parseInt(data[0]);
            String genreName = data[1];

            film.addGenre(new Genre(genreId, genreName));
        }

        genresDataResultSet.close();

        return film;
    }
}