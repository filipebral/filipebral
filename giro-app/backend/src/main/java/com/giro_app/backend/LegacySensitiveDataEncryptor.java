package com.giro_app.backend;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LegacySensitiveDataEncryptor implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacySensitiveDataEncryptor.class);
    private static final List<String> COLUMNS = List.of(
            "baas_account_id",
            "routing_number",
            "branch",
            "account_number");
    private static final int BATCH_SIZE = 500;

    private final JdbcTemplate jdbcTemplate;
    private final SensitiveDataConverter converter;

    public LegacySensitiveDataEncryptor(JdbcTemplate jdbcTemplate, SensitiveDataConverter converter) {
        this.jdbcTemplate = jdbcTemplate;
        this.converter = converter;
    }

    @Override
    public void run(ApplicationArguments args) {
        int migrated = 0;
        for (String column : COLUMNS) {
            while (true) {
                List<LegacyValue> batch = jdbcTemplate.query(
                        "SELECT id, " + column + " FROM accounts "
                                + "WHERE " + column + " IS NOT NULL AND " + column + " NOT LIKE 'enc:v1:%' "
                                + "ORDER BY id LIMIT ?",
                        (resultSet, rowNumber) -> new LegacyValue(
                                resultSet.getObject("id"),
                                resultSet.getString(column)),
                        BATCH_SIZE);
                if (batch.isEmpty()) {
                    break;
                }
                for (LegacyValue value : batch) {
                    jdbcTemplate.update(
                            "UPDATE accounts SET " + column + " = ? WHERE id = ?",
                            converter.convertToDatabaseColumn(value.value()),
                            value.id());
                }
                migrated += batch.size();
            }
        }
        if (migrated > 0) {
            LOGGER.info("Encrypted {} legacy sensitive account fields.", migrated);
        }
    }

    private record LegacyValue(Object id, String value) {
    }
}
