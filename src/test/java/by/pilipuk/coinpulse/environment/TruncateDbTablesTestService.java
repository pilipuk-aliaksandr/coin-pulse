package by.pilipuk.coinpulse.environment;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.List;

@Service
public class TruncateDbTablesTestService {

    public static final List<String> TABLES_NAMES = List.of("coins.prices");
    public static final String TRUNCATE_TABLES =
            """
                    TRUNCATE TABLE %s RESTART IDENTITY CASCADE;
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TruncateDbTablesTestService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void truncateAllTables() {
        TABLES_NAMES.forEach(table -> jdbcTemplate.execute(
                TRUNCATE_TABLES.formatted(table),
                PreparedStatement::execute));
    }
}
