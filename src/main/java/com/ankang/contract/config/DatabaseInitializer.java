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
        migrateWasteRecordStaffNos();
        fixHospitalRegionCodes();
        addStatusColumns();
        
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

    /**
     * 将waste_record表的staff_no列迁移为staff_nos（支持多人员选择）
     */
    private void migrateWasteRecordStaffNos() {
        try {
            // 检查waste_record表是否存在旧列staff_no
            boolean hasOldColumn = jdbcTemplate.query(
                "SHOW COLUMNS FROM waste_record LIKE 'staff_no'", (ResultSet rs) -> rs.next());
            if (hasOldColumn) {
                // 删除外键约束（如果存在）
                try {
                    jdbcTemplate.execute("ALTER TABLE waste_record DROP FOREIGN KEY fk_record_staff");
                } catch (Exception ignored) {
                    // 外键可能不存在，忽略
                }
                // 将staff_no列重命名为staff_nos，并扩大长度
                jdbcTemplate.execute(
                    "ALTER TABLE waste_record CHANGE COLUMN staff_no staff_nos VARCHAR(30) NOT NULL COMMENT '人员编号（逗号分隔）'");
                // 更新索引
                try {
                    jdbcTemplate.execute("ALTER TABLE waste_record DROP INDEX idx_staff_no");
                } catch (Exception ignored) {
                    // 索引可能不存在
                }
                try {
                    jdbcTemplate.execute("ALTER TABLE waste_record ADD INDEX idx_staff_nos (staff_nos)");
                } catch (Exception ignored) {
                    // 索引可能已存在
                }
                logger.info("Migrated waste_record.staff_no to staff_nos");
            }
        } catch (Exception e) {
            logger.warn("Could not migrate waste_record staff column: {}", e.getMessage());
        }
    }

    /**
     * 修复waste_hospital表中地区字段存储中文名称而非编码的问题
     */
    private void fixHospitalRegionCodes() {
        try {
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'XFQ' WHERE region = '忻府区'");
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'YPS' WHERE region = '原平市'");
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'DXX' WHERE region = '定襄县'");
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'WTX' WHERE region = '五台县'");
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'DZX' WHERE region = '代县'");
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'FSX' WHERE region = '繁峙县'");
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'JLX' WHERE region = '静乐县'");
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'NWX' WHERE region = '宁武县'");
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'SCX' WHERE region = '神池县'");
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'WZX' WHERE region = '五寨县'");
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'KLX' WHERE region = '岢岚县'");
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'BDX' WHERE region = '保德县'");
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'HQX' WHERE region = '河曲县'");
            jdbcTemplate.execute("UPDATE waste_hospital SET region = 'PGX' WHERE region = '偏关县'");
            logger.info("Fixed hospital region codes from Chinese names to codes");
        } catch (Exception e) {
            logger.warn("Could not fix hospital region codes: {}", e.getMessage());
        }
    }

    /**
     * 为waste_vehicle、waste_staff、waste_hospital三表新增status字段（逻辑删除）
     */
    private void addStatusColumns() {
        String[] tables = {"waste_vehicle", "waste_staff", "waste_hospital"};
        for (String table : tables) {
            try {
                boolean hasColumn = jdbcTemplate.query(
                    "SHOW COLUMNS FROM " + table + " LIKE 'status'", (ResultSet rs) -> rs.next());
                if (!hasColumn) {
                    jdbcTemplate.execute("ALTER TABLE " + table
                        + " ADD COLUMN status TINYINT NOT NULL DEFAULT 1 COMMENT '1-normal,0-deleted'");
                    jdbcTemplate.execute("ALTER TABLE " + table + " ADD INDEX idx_status (status)");
                    logger.info("Added 'status' column to {} table", table);
                }
            } catch (Exception e) {
                logger.warn("Could not add 'status' column to {}: {}", table, e.getMessage());
            }
        }
    }
}