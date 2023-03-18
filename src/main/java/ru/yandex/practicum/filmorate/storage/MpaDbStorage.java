package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

@Repository
@Slf4j
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Mpa> mpaMapper;

    public MpaDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Mpa> mpaMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaMapper = mpaMapper;
    }


    @Override
    public Collection<Mpa> getAll() {
        String sql = "SELECT * FROM ratings ORDER BY id;";

        log.info("Получен список всех рейтингов из базы");
        return jdbcTemplate.query(sql, mpaMapper);
    }

    @Override
    public Mpa getById(int mpaId) {
        String sql = "SELECT * FROM ratings WHERE id = ?;";

        log.info("Рейтинг с id = {} получен из базы", mpaId);
        return jdbcTemplate.queryForObject(sql, mpaMapper, mpaId);
    }

    @Override
    public boolean isRatingExists(int mpaId) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT * FROM ratings WHERE id = ?;", mpaId);

        log.info("Получена информация о наличии рейтинга с id = {} в базе", mpaId);
        return rowSet.next();
    }
}
