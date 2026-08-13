package salpim.umc10thsalpim.domain.auth.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.enums.PasswordVerificationTargetType;
import salpim.umc10thsalpim.domain.auth.repository.PasswordVerificationAttemptRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PasswordVerificationAttemptMappingTest {

    @Autowired
    private PasswordVerificationAttemptRepository attemptRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void storesLoginIpAttemptInVarcharColumns() {
        PasswordVerificationAttempt attempt = PasswordVerificationAttempt.create(
                PasswordVerificationPurpose.LOGIN,
                PasswordVerificationTargetType.IP_ADDRESS,
                "2001:db8::1"
        );

        PasswordVerificationAttempt saved = attemptRepository.saveAndFlush(attempt);

        assertThat(saved.getId()).isNotNull();
        assertVarcharColumn("purpose");
        assertVarcharColumn("target_type");
    }

    private void assertVarcharColumn(String columnName) {
        Map<String, Object> column = jdbcTemplate.queryForMap("""
                select data_type, character_maximum_length
                from information_schema.columns
                where table_name = 'password_verification_attempt'
                  and column_name = ?
                """, columnName);

        assertThat(column.get("data_type").toString()).containsIgnoringCase("character varying");
        assertThat(((Number) column.get("character_maximum_length")).longValue()).isEqualTo(30L);
    }
}
