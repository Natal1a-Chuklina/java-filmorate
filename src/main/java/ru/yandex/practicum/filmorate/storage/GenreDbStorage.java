package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Repository
@Slf4j
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Genre> genreMapper;

    public GenreDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Genre> genreMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreMapper = genreMapper;
    }

    @Override
    public Collection<Genre> getAll() {
        String sql = "SELECT * FROM genres ORDER BY id;";

        log.info("Получен список всех жанров из базы");
        return jdbcTemplate.query(sql, genreMapper);
    }

    @Override
    public Genre getById(int genreId) {
        String sql = "SELECT * FROM genres WHERE id = ?;";

        log.info("Жанр с id = {} получен из базы", genreId);
        return jdbcTemplate.queryForObject(sql, genreMapper, genreId);
    }

    @Override
    public boolean isGenreExists(int genreId) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT * FROM genres WHERE id = ?;", genreId);

        log.info("Получена информация о наличии жанра с id = {} в базе", genreId);
        return rowSet.next();
    }
}
