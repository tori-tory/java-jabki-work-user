package ru.jabki.work.user.repository;

import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.jabki.work.user.exception.ConflictException;
import ru.jabki.work.user.model.User;
import ru.jabki.work.user.repository.mapper.UserMapper;

import java.util.List;

@Repository
@AllArgsConstructor
public class UserRepository {
    private static final String INSERT = """
        INSERT INTO work_user."user"(username, password)
        VALUES (:username, :password)
        RETURNING id
        """;

    private static final String SOFT_DELETE = """
            UPDATE work_user."user"
            SET deleted_at = now()
            WHERE id = :id
            AND deleted_at is null
            """;

    private static final String GET_BY_ID = """
            SELECT id, username
            FROM work_user."user"
            WHERE id = :id
            AND deleted_at is null
            """;

    private static final String EXISTS_BY_ID =  """
            SELECT EXISTS (
                SELECT 1
                FROM work_user."user"
                WHERE id = :id
                AND deleted_at is null
            )
            """;

    private static final String GET_ALL = """
            SELECT id, username
            FROM work_user."user"
            WHERE deleted_at is null
            """;

    private final UserMapper userMapper;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Long insert(final User user){
        try {
            return jdbcTemplate.queryForObject(INSERT, userToSql(user), Long.class);
        } catch (DuplicateKeyException e) {
            throw new ConflictException(String.format("Нарушение уникальности. Пользователь %s уже существует", user.getUsername()));
        }
    }

    public User getById(final Long id) {
        try {
            return jdbcTemplate.queryForObject(GET_BY_ID, new MapSqlParameterSource("id", id), userMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public boolean existsById(final Long id) {
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(EXISTS_BY_ID, new MapSqlParameterSource("id", id), Boolean.class));
    }

    public int softDelete(final Long id) {
        return jdbcTemplate.update(SOFT_DELETE, new MapSqlParameterSource("id", id));
    }

    public List<User> getAll(){
        return jdbcTemplate.query(GET_ALL, userMapper);
    }

    private MapSqlParameterSource userToSql(final User user) {
        final MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("username", user.getUsername());
        params.addValue("password", user.getPassword());
        return params;
    }
}