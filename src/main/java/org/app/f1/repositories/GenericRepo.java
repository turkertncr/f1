package org.app.f1.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GenericRepo {

    private final JdbcTemplate jdbcTemplate;

    public <T> void batchInsertIgnore(String sql, List<T> items , BiCustomSetter<T> mapper) {
        if (items == null || items.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(sql, new  BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                mapper.setValues(ps, items.get(i));
            }
            @Override
            public int getBatchSize() {
                return items.size();
            }
        });
    }

    @FunctionalInterface
    public interface BiCustomSetter<T> {
        void setValues(PreparedStatement ps, T item) throws SQLException;
    }
}
