package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Film> filmMapper;
    private final RowMapper<Genre> genreMapper;
    private final RowMapper<Mpa> mpaMapper;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Film> filmMapper, RowMapper<Genre> genreMapper,
                         RowMapper<Mpa> mpaMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmMapper = filmMapper;
        this.genreMapper = genreMapper;
        this.mpaMapper = mpaMapper;
    }

    @Override
    public Collection<Film> getAll() {
        String sql =
                "SELECT f.id, " +
                        "       f.name, " +
                        "       f.description, " +
                        "       f.release_date, " +
                        "       f.duration, " +
                        "       r.id AS rating_id, " +
                        "       r.name AS rating_name, " +
                        "       array_agg(DISTINCT f_g.genre_id || ' ' || g.name ORDER BY f_g.genre_id) AS genres_data, " +
                        "       array_agg(DISTINCT l.user_id ORDER BY l.user_id) AS likes_data, " +
                        "       array_agg(DISTINCT f_d.director_id || ',' || d.director_name ORDER BY f_d.director_id) AS directors_data " +
                        "FROM films AS f " +
                        "LEFT JOIN ratings AS r ON r.id = f.rating_id " +
                        "LEFT JOIN film_genre AS f_g ON f_g.film_id = f.id " +
                        "LEFT JOIN genres AS g ON g.id = f_g.genre_id " +
                        "LEFT JOIN likes AS l ON f.id = l.film_id " +
                        "LEFT JOIN film_director AS f_d ON f_d.film_id = f.id " +
                        "LEFT JOIN director AS d ON d.director_id = f_d.director_id " +
                        "GROUP BY f.id;";

        log.info("Получен список всех фильмов из базы");
        return jdbcTemplate.query(sql, filmMapper);
    }

    @Override
    public boolean isFilmExists(int filmId) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?;", filmId);

        log.info("Получена информация о наличии фильма с id = {} в базе", filmId);
        return rowSet.next();
    }

    @Override
    public boolean isFilmContainsUserLike(int filmId, int userId) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT * FROM likes WHERE film_id = ? AND user_id = ?;",
                filmId, userId);

        log.info("Получена информация о наличии у фильма с id = {} лайка от пользователя с id = {}", filmId, userId);
        return rowSet.next();
    }

    @Override
    public int add(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");

        int filmId = simpleJdbcInsert.executeAndReturnKey(film.toMap()).intValue();

        addGenres(film.getGenres(), filmId);
        addMpaName(film.getMpa());
        addDirectors(filmId, film.getDirectors());

        log.info("В базу добавлен фильм с id = {}", filmId);
        return filmId;
    }

    @Override
    public void update(Film film) {
        String sql =
                "UPDATE films " +
                        "SET name = ?, " +
                        "    description = ?, " +
                        "    release_date = ?, " +
                        "    duration = ?, " +
                        "    rating_id = ? " +
                        "WHERE id = ?;";

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        deleteGenres(film.getId());
        addGenres(film.getGenres(), film.getId());
        addDirectors(film.getId(), film.getDirectors());

        addMpaName(film.getMpa());
        log.info("Информация о фильме с id = {} обновлена в базе", film.getId());
    }

    @Override
    public Film getFilm(int filmId) {
        String sql =
                "SELECT f.id, " +
                        "       f.name, " +
                        "       f.description, " +
                        "       f.release_date, " +
                        "       f.duration, " +
                        "       r.id AS rating_id, " +
                        "       r.name AS rating_name, " +
                        "       array_agg(DISTINCT f_g.genre_id || ' ' || g.name ORDER BY f_g.genre_id) AS genres_data, " +
                        "       array_agg(DISTINCT l.user_id ORDER BY l.user_id) AS likes_data, " +
                        "       array_agg(DISTINCT f_d.director_id || ',' || d.director_name ORDER BY f_d.director_id) AS directors_data " +
                        "FROM films AS f " +
                        "LEFT JOIN ratings AS r ON r.id = f.rating_id " +
                        "LEFT JOIN film_genre AS f_g ON f_g.film_id = f.id " +
                        "LEFT JOIN genres AS g ON g.id = f_g.genre_id " +
                        "LEFT JOIN likes AS l ON f.id = l.film_id " +
                        "LEFT JOIN film_director AS f_d ON f_d.film_id = f.id " +
                        "LEFT JOIN director AS d ON d.director_id = f_d.director_id " +
                        "WHERE f.id = ? " +
                        "GROUP BY f.id;";

        log.info("Фильм с id = {} получен из базы", filmId);
        return jdbcTemplate.queryForObject(sql, filmMapper, filmId);
    }

    @Override
    public void delete(int filmId) {

        final String sqlQuery = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sqlQuery, filmId);
        log.info("Фильм с id = {} удалён ", filmId);

    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql =
                "INSERT INTO likes (film_id, user_id)" +
                        "VALUES (?, ?);";

        jdbcTemplate.update(sql, filmId, userId);
        log.info("Пользователь с id = {} поставил лайк фильму с id = {}", userId, filmId);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        String sql =
                "DELETE " +
                        "FROM likes " +
                        "WHERE film_id = ?" +
                        "  AND user_id = ?;";

        jdbcTemplate.update(sql, filmId, userId);
        log.info("Пользователь с id = {} удалил лайк у фильма с id = {}", userId, filmId);
    }

    @Override
    public Collection<Film> getBestFilms(int count) {
        String sql =
                "SELECT f.id, " +
                        "       f.name, " +
                        "       f.description, " +
                        "       f.release_date, " +
                        "       f.duration, " +
                        "       r.id AS rating_id, " +
                        "       r.name AS rating_name, " +
                        "       array_agg(DISTINCT f_g.genre_id || ' ' || g.name ORDER BY f_g.genre_id) AS genres_data, " +
                        "       array_agg(DISTINCT l.user_id ORDER BY l.user_id) AS likes_data, " +
                        "       array_agg(DISTINCT f_d.director_id || ',' || d.director_name ORDER BY f_d.director_id) AS directors_data " +
                        "FROM films AS f " +
                        "LEFT JOIN ratings AS r ON r.id = f.rating_id " +
                        "LEFT JOIN film_genre AS f_g ON f_g.film_id = f.id " +
                        "LEFT JOIN genres AS g ON g.id = f_g.genre_id " +
                        "LEFT JOIN likes AS l ON l.film_id = f.id " +
                        "LEFT JOIN film_director AS f_d ON f.id = f_d.film_id " +
                        "LEFT JOIN director AS d ON d.director_id = f_d.director_id " +
                        "GROUP BY f.id " +
                        "ORDER BY count(DISTINCT(l.user_id)) DESC, " +
                        "         f.name " +
                        "LIMIT ?;";

        log.info("Получен список топ {} фильмов из базы", count);
        return jdbcTemplate.query(sql, filmMapper, count);
    }

    @Override
    public List<Film> getSortedFilmsByDirId(long directorId, String sort) {
        String sqlQuery = "SELECT f.id, " +
                "       f.name, " +
                "       f.description, " +
                "       f.release_date, " +
                "       f.duration, " +
                "       r.id AS rating_id, " +
                "       r.name AS rating_name, " +
                "       array_agg(DISTINCT f_g.genre_id || ' ' || g.name ORDER BY f_g.genre_id) AS genres_data, " +
                "       array_agg(DISTINCT l.user_id ORDER BY l.user_id) AS likes_data, " +
                "       array_agg(DISTINCT f_d.director_id || ',' || d.director_name ORDER BY f_d.director_id) AS directors_data " +
                "FROM films AS f " +
                "LEFT JOIN ratings AS r ON r.id = f.rating_id " +
                "LEFT JOIN film_genre AS f_g ON f_g.film_id = f.id " +
                "LEFT JOIN genres AS g ON g.id = f_g.genre_id " +
                "LEFT JOIN likes AS l ON l.film_id = f.id " +
                "LEFT JOIN film_director AS f_d ON f.id = f_d.film_id " +
                "LEFT JOIN director AS d ON d.director_id = f_d.director_id " +
                "WHERE f_d.director_id = ? " +
                "GROUP BY f.id ";
        switch (sort) {
            case "likes":
                sqlQuery = sqlQuery + "ORDER BY COUNT(DISTINCT l.user_id) DESC, f.name;";
                break;
            case "year":
                sqlQuery = sqlQuery + "ORDER BY f.release_date, f.name;";
                break;
        }
        log.info("Получен список фильмов режиссера с id = {}, отсортированный по {}", directorId, sort);
        return jdbcTemplate.query(sqlQuery, filmMapper, directorId);
    }

    @Override
    public Collection<Film> getCommonFilms(int userId, int friendId) {
        String sql =
                "SELECT f.id, " +
                        "       f.name, " +
                        "       f.description, " +
                        "       f.release_date, " +
                        "       f.duration, " +
                        "       r.id AS rating_id, " +
                        "       r.name AS rating_name, " +
                        "       array_agg(DISTINCT f_g.genre_id || ' ' || g.name ORDER BY f_g.genre_id) AS genres_data, " +
                        "       array_agg(DISTINCT l.user_id ORDER BY l.user_id) AS likes_data, " +
                        "       array_agg(DISTINCT f_d.director_id || ',' || d.director_name ORDER BY f_d.director_id) AS directors_data " +
                        "FROM films AS f " +
                        "LEFT JOIN ratings AS r ON r.id = f.rating_id " +
                        "LEFT JOIN film_genre AS f_g ON f_g.film_id = f.id " +
                        "LEFT JOIN genres AS g ON g.id = f_g.genre_id " +
                        "LEFT JOIN likes AS l ON l.film_id = f.id " +
                        "LEFT JOIN film_director AS f_d ON f.id = f_d.film_id " +
                        "LEFT JOIN director AS d ON d.director_id = f_d.director_id " +
                        "WHERE f.id in " +
                        "      (SELECT l_1.film_id " +
                        "       FROM likes AS l_1 " +
                        "       INNER JOIN " +
                        "         (SELECT film_id " +
                        "          FROM likes " +
                        "          WHERE user_id = ?) AS l_2 ON l_1.film_id = l_2.film_id " +
                        "       WHERE l_1.user_id = ?) " +
                        "GROUP BY f.id " +
                        "ORDER BY count(DISTINCT(l.user_id)) DESC, " +
                        "         f.name;";

        log.info("Получен список общих любимых фильмов пользователей с id: {} и {}", userId, friendId);
        return jdbcTemplate.query(sql, filmMapper, userId, friendId);
    }

    @Override
    public List<Film> getSortedFilmByQuery(String query, String by) {
        query = "%" + query + "%";
        String sqlQuery = "SELECT f.id, " +
                "       f.name, " +
                "       f.description, " +
                "       f.release_date, " +
                "       f.duration, " +
                "       r.id AS rating_id, " +
                "       r.name AS rating_name, " +
                "       array_agg(DISTINCT f_g.genre_id || ' ' || g.name ORDER BY f_g.genre_id) AS genres_data, " +
                "       array_agg(DISTINCT l.user_id ORDER BY l.user_id) AS likes_data, " +
                "       array_agg(DISTINCT f_d.director_id || ',' || d.director_name ORDER BY f_d.director_id) AS directors_data " +
                "FROM films AS f " +
                "LEFT JOIN ratings AS r ON r.id = f.rating_id " +
                "LEFT JOIN film_genre AS f_g ON f_g.film_id = f.id " +
                "LEFT JOIN genres AS g ON g.id = f_g.genre_id " +
                "LEFT JOIN likes AS l ON l.film_id = f.id " +
                "LEFT JOIN film_director AS f_d ON f.id = f_d.film_id " +
                "LEFT JOIN director AS d ON d.director_id = f_d.director_id ";
        switch (by) {
            case "director":
                sqlQuery = sqlQuery +
                        "WHERE LOWER(D.DIRECTOR_NAME) LIKE LOWER(?) " +
                        "GROUP BY F.ID ORDER BY D.DIRECTOR_NAME";
                log.info("Получены фильмы, отсортированные по {}, имеющих подстроку {}", by, query);
                return jdbcTemplate.query(sqlQuery, filmMapper, query);
            case "title":
                sqlQuery = sqlQuery +
                        "WHERE LOWER(F.NAME) LIKE LOWER(?) " +
                        "GROUP BY F.ID ORDER BY F.NAME";
                log.info("Получены фильмы, отсортированные по {}, имеющих подстроку {}", by, query);
                return jdbcTemplate.query(sqlQuery, filmMapper, query);
            case "director,title":
                sqlQuery = sqlQuery +
                        "WHERE LOWER(D.DIRECTOR_NAME) LIKE LOWER(?) OR LOWER(F.NAME) LIKE LOWER(?) " +
                        "GROUP BY F.ID ORDER BY D.DIRECTOR_NAME, F.NAME";
                break;
            case "title,director":
                sqlQuery = sqlQuery +
                        "WHERE LOWER(F.NAME) LIKE LOWER(?) OR LOWER(D.DIRECTOR_NAME) LIKE LOWER(?) " +
                        "GROUP BY F.ID ORDER BY F.NAME DESC, D.DIRECTOR_NAME";
                break;
        }
        log.info("Получены фильмы, отсортированные по {}, имеющих подстроку {}", by, query);
        return jdbcTemplate.query(sqlQuery, filmMapper, query, query);
    }

    private void addGenres(Set<Genre> genres, int filmId) {
        for (Genre genre : genres) {
            String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?);";
            jdbcTemplate.update(sql, filmId, genre.getId());
            log.info("Фильму с id = {} добавлен жанр с id = {}", filmId, genre.getId());

            Genre genreWithName = jdbcTemplate.queryForObject("SELECT * FROM genres WHERE id = ?;",
                    genreMapper, genre.getId());
            genre.setName(genreWithName.getName());
        }
    }

    private void addMpaName(Mpa mpa) {
        Mpa mpaWithName = jdbcTemplate.queryForObject("SELECT * FROM ratings WHERE id = ?;",
                mpaMapper, mpa.getId());
        mpa.setName(mpaWithName.getName());
    }


    private void deleteGenres(int filmId) {
        String sql = "DELETE FROM film_genre WHERE film_id = ?;";
        jdbcTemplate.update(sql, filmId);
        log.info("Удалены все жанры фильма с id = {}", filmId);
    }

    private void addDirectors(long filmId, Set<Director> directors) {
        String sqlQuery = "DELETE FROM FILM_DIRECTOR WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, filmId);
        log.info("Удалены все режиссеры фильма с id = {}", filmId);
        if (directors == null || directors.isEmpty()) {
            return;
        }
        List<Director> directorListWithoutDuplicate = new ArrayList<>(directors);
        jdbcTemplate.batchUpdate("INSERT INTO FILM_DIRECTOR (DIRECTOR_ID, FILM_ID) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, directorListWithoutDuplicate.get(i).getId());
                        ps.setLong(2, filmId);
                    }

                    @Override
                    public int getBatchSize() {
                        return directorListWithoutDuplicate.size();
                    }
                });
        log.info("Добавлены режиссеры - {} в фильм с id = {}", directors, filmId);
    }
}
