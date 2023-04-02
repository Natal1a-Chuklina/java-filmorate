package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;
    private final ReviewMapper reviewMapper;


    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews(content, isPositive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());
            stmt.setInt(5, 0);
            return stmt;
        }, keyHolder);

        int reviewId = Objects.requireNonNull(keyHolder.getKey()).intValue();

        review.setReviewId(reviewId);
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, isPositive = ? WHERE id = ?";

        jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        return findReviewById(review.getReviewId());
    }

    @Override
    public int remove(Integer id) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public Review findReviewById(Integer id) {
        String sql = "SELECT * FROM reviews WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, reviewMapper, id);
    }

    @Override
    public List<Review> findAll() {
        String sql = "SELECT * FROM reviews ORDER BY useful DESC";
        return jdbcTemplate.query(sql, reviewMapper);
    }

    @Override
    public List<Review> findReviewsByFilmId(Integer filmId, Integer count) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, reviewMapper, filmId, count);
    }

    @Override
    public void addLike(Integer reviewId, Integer userId) {
        String sql = "INSERT INTO reviews_likes (review_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, reviewId, userId);

        String sqlForReview = "UPDATE reviews SET useful = useful + 1 WHERE id = ?";
        jdbcTemplate.update(sqlForReview, reviewId);
    }

    @Override
    public void addDislike(Integer reviewId, Integer userId) {
        String sql = "INSERT INTO reviews_dislikes (review_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, reviewId, userId);

        String sqlForReview = "UPDATE reviews SET useful = useful - 1 WHERE id = ?";
        jdbcTemplate.update(sqlForReview, reviewId);
    }

    @Override
    public void removeLike(Integer reviewId, Integer userId) {
        String sql = "DELETE FROM reviews_likes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, reviewId, userId);

        String sqlForReview = "UPDATE reviews SET useful = useful - 1 WHERE id = ?";
        jdbcTemplate.update(sqlForReview, reviewId);
    }

    @Override
    public void removeDislike(Integer reviewId, Integer userId) {
        String sql = "DELETE FROM reviews_dislikes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, reviewId, userId);

        String sqlForReview = "UPDATE reviews SET useful = useful + 1 WHERE id = ?";
        jdbcTemplate.update(sqlForReview, reviewId);
    }

    @Override
    public boolean isReviewExists(Integer id) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet("SELECT * FROM reviews WHERE id = ?", id);
        return rowSet.next();
    }

    @Override
    public boolean hasUserAlreadyLeftLikeForFilm(Integer reviewId, Integer userId) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(
                "SELECT * FROM reviews_likes WHERE review_id = ? AND user_id = ?", reviewId, userId);
        return rowSet.next();
    }

    @Override
    public boolean hasUserAlreadyLeftDislikeForFilm(Integer reviewId, Integer userId) {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(
                "SELECT * FROM reviews_dislikes WHERE review_id = ? AND user_id = ?", reviewId, userId);
        return rowSet.next();
    }
}
