package com.ankang.contract.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void run(String... args) throws Exception {
        initializeCreatedByColumn();
        dropContractColumns();
        
        String correctHash = "$2a$10$oNlJ5uqG3D0q1bACGGpItOV/Lj.QexJ7nLYMwuRRGOCRFmsHWIgSK";
        
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys_user WHERE username = 'admin'",
            Integer.class
        );
        
        if (count != null && count > 0) {
            jdbcTemplate.update(
                "UPDATE sys_user SET password = ? WHERE username = 'admin'",
                correctHash
            );
            System.out.println("Admin password updated successfully!");
        }
    }

    private void dropContractColumns() {
        try {
            boolean hasContractName = jdbcTemplate.query("SHOW COLUMNS FROM contract LIKE 'contract_name'", (ResultSet rs) -> rs.next());
            if (hasContractName) {
                jdbcTemplate.execute("ALTER TABLE contract DROP COLUMN contract_name");
                logger.info("Dropped 'contract_name' column from contract table");
            }
            
            boolean hasPaymentMethod = jdbcTemplate.query("SHOW COLUMNS FROM contract LIKE 'payment_method'", (ResultSet rs) -> rs.next());
            if (hasPaymentMethod) {
                jdbcTemplate.execute("ALTER TABLE contract DROP COLUMN payment_method");
                logger.info("Dropped 'payment_method' column from contract table");
            }
            
            boolean hasPaymentCycle = jdbcTemplate.query("SHOW COLUMNS FROM contract LIKE 'payment_cycle'", (ResultSet rs) -> rs.next());
            if (hasPaymentCycle) {
                jdbcTemplate.execute("ALTER TABLE contract DROP COLUMN payment_cycle");
                logger.info("Dropped 'payment_cycle' column from contract table");
            }
            
            boolean hasContractType = jdbcTemplate.query("SHOW COLUMNS FROM contract LIKE 'contract_type'", (ResultSet rs) -> rs.next());
            if (hasContractType) {
                jdbcTemplate.execute("ALTER TABLE contract DROP COLUMN contract_type");
                logger.info("Dropped 'contract_type' column from contract table");
            }
        } catch (Exception e) {
            logger.warn("Could not drop contract columns: {}", e.getMessage());
        }
    }

    private void initializeCreatedByColumn() {
        try {
            boolean columnExists = jdbcTemplate.query("SHOW COLUMNS FROM institution LIKE 'created_by'", (ResultSet rs) -> rs.next());
            if (!columnExists) {
                jdbcTemplate.execute("ALTER TABLE institution ADD COLUMN created_by VARCHAR(50) COMMENT '录入人' AFTER institution_status");
                jdbcTemplate.execute("ALTER TABLE institution ADD INDEX idx_created_by (created_by)");
                logger.info("Created 'created_by' column in institution table");
            }
        } catch (Exception e) {
            logger.warn("Could not add 'created_by' column: {}", e.getMessage());
        }
    }
}