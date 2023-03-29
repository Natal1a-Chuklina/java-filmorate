package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Director getDirectorById(long id) {
        String sqlQuery = "SELECT * FROM DIRECTOR WHERE DIRECTOR_ID = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::mapRowToDirector, id);
    }

    @Override
    public List<Director> getAllDirectors() {
        String sqlQuery = "SELECT * FROM DIRECTOR";
        return jdbcTemplate.query(sqlQuery, this::mapRowToDirector);
    }

    @Override
    public Director addDirector(Director director) {
        String sqlQuery = "INSERT INTO DIRECTOR(DIRECTOR_NAME) VALUES (?)";
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, generatedKeyHolder);
        Long directorId = generatedKeyHolder.getKey().longValue();
        director.setId(directorId);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String sqlQuery = "UPDATE DIRECTOR SET DIRECTOR_NAME = ? WHERE DIRECTOR_ID = ?";
        jdbcTemplate.update(sqlQuery,
                director.getName(),
                director.getId());
        return director;
    }

    @Override
    public void deleteDirector(long id) {
        String sqlQuery = "DELETE FROM DIRECTOR WHERE DIRECTOR_ID = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    @Override
    public boolean directorExists(long id) {
        String sqlQuery = "SELECT COUNT(*) FROM DIRECTOR WHERE DIRECTOR_ID = ?";
        int result = jdbcTemplate.queryForObject(sqlQuery, Integer.class, id);
        return result == 1;
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        return Director.builder()
                .id(rs.getLong("DIRECTOR_ID"))
                .name(rs.getString("DIRECTOR_NAME"))
                .build();
    }
}
