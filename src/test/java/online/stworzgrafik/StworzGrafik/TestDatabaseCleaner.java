package online.stworzgrafik.StworzGrafik;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    public void cleanAll() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE schedule_details");
        jdbcTemplate.execute("TRUNCATE TABLE schedule_message");
        jdbcTemplate.execute("TRUNCATE TABLE schedule");
        jdbcTemplate.execute("TRUNCATE TABLE employee_proposal_days_off");
        jdbcTemplate.execute("TRUNCATE TABLE employee");
        jdbcTemplate.execute("TRUNCATE TABLE shift_type_config");
        jdbcTemplate.execute("TRUNCATE TABLE shift");
        jdbcTemplate.execute("TRUNCATE TABLE store");
        jdbcTemplate.execute("TRUNCATE TABLE branch");
        jdbcTemplate.execute("TRUNCATE TABLE position");
        jdbcTemplate.execute("TRUNCATE TABLE region");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }
}
