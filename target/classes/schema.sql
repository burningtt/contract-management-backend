-- 医疗机构合同管理系统数据库表创建脚本

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN-管理员，USER-普通用户',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 机构表（原客户表）
CREATE TABLE IF NOT EXISTS institution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '机构ID',
    institution_no VARCHAR(6) NOT NULL UNIQUE COMMENT '机构编号（6位数字，从100001开始）',
    institution_name VARCHAR(100) NOT NULL COMMENT '机构名称',
    institution_type VARCHAR(50) NOT NULL COMMENT '机构类型：HOSPITAL-医院，CLINIC-诊所，COMMUNITY-社区卫生服务中心，PHARMACY-药店，LABORATORY-检验中心，HEALTH_CENTER-卫生院，HEALTH_STATION-卫生所，OTHER-其他',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    region VARCHAR(50) COMMENT '地区：忻府区、原平市、定襄县、五台县、代县、繁峙县、静乐县、宁武县、神池县、五寨县、岢岚县、保德县、河曲县、偏关县',
    address VARCHAR(200) COMMENT '详细地址',
    institution_status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '机构状态：NORMAL-正常，PAUSED-暂停合作，TERMINATED-已解约',
    created_by VARCHAR(50) COMMENT '录入人',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_institution_no (institution_no),
    INDEX idx_institution_name (institution_name),
    INDEX idx_institution_type (institution_type),
    INDEX idx_institution_status (institution_status),
    INDEX idx_region (region),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医疗机构信息表';

-- 合同表
CREATE TABLE IF NOT EXISTS contract (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '合同ID',
    contract_no VARCHAR(50) NOT NULL UNIQUE COMMENT '合同编号',
    institution_no VARCHAR(6) NOT NULL COMMENT '机构编号',
    contract_amount DECIMAL(18, 2) NOT NULL COMMENT '合同金额',
    sign_date DATE COMMENT '签订日期',
    expire_date DATE COMMENT '到期日期',
    responsible_person VARCHAR(50) COMMENT '负责人',
    contract_status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '合同状态：active-活跃，inactive-失效，pending-未生效',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_contract_no (contract_no),
    INDEX idx_institution_no (institution_no),
    INDEX idx_contract_status (contract_status),
    INDEX idx_sign_date (sign_date),
    CONSTRAINT fk_contract_institution FOREIGN KEY (institution_no) REFERENCES institution(institution_no) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同信息表';

-- 合同费用记录表
CREATE TABLE IF NOT EXISTS contract_fee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '费用记录ID',
    institution_no VARCHAR(6) NOT NULL COMMENT '机构编号',
    fee_amount DECIMAL(18, 2) NOT NULL COMMENT '费用金额',
    fee_date DATE NOT NULL COMMENT '费用日期',
    payment_method VARCHAR(50) COMMENT '支付方式',
    recorded_by VARCHAR(50) COMMENT '录入人',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_institution_no (institution_no),
    INDEX idx_fee_date (fee_date),
    CONSTRAINT fk_fee_institution FOREIGN KEY (institution_no) REFERENCES institution(institution_no) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合同费用记录表';

-- 医废车辆表
CREATE TABLE IF NOT EXISTS waste_vehicle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '车辆ID',
    vehicle_no VARCHAR(10) NOT NULL UNIQUE COMMENT '车辆编号（如V00001）',
    plate_number VARCHAR(20) NOT NULL COMMENT '车牌号',
    mileage DECIMAL(10,1) COMMENT '车辆里程（公里）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_vehicle_no (vehicle_no),
    INDEX idx_plate_number (plate_number),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医废车辆信息表';

-- 车队人员表
CREATE TABLE IF NOT EXISTS waste_staff (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '人员ID',
    staff_no VARCHAR(10) NOT NULL UNIQUE COMMENT '人员编号（如S00001）',
    staff_name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) COMMENT '电话',
    id_card VARCHAR(18) COMMENT '证件号',
    position VARCHAR(50) COMMENT '岗位',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_staff_no (staff_no),
    INDEX idx_staff_name (staff_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车队人员信息表';

-- 医废医院表
CREATE TABLE IF NOT EXISTS waste_hospital (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '医院ID',
    hospital_no VARCHAR(10) NOT NULL UNIQUE COMMENT '医院编号（如H00001）',
    hospital_name VARCHAR(100) NOT NULL COMMENT '医院名称',
    region VARCHAR(50) COMMENT '地区编码（忻州市14个区县编码）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，0-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_hospital_no (hospital_no),
    INDEX idx_hospital_name (hospital_name),
    INDEX idx_region (region),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医废医院信息表';

-- 医废重量记录表
CREATE TABLE IF NOT EXISTS waste_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    batch_no VARCHAR(20) NOT NULL UNIQUE COMMENT '批次编号（如YF20260522001）',
    hospital_no VARCHAR(10) NOT NULL COMMENT '医院编号',
    region VARCHAR(50) COMMENT '地区编码',
    vehicle_no VARCHAR(10) NOT NULL COMMENT '车辆编号',
    staff_nos VARCHAR(30) NOT NULL COMMENT '人员编号（逗号分隔，如S00001,S00002）',
    infectious_weight DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '感染性重量(kg)',
    injurious_weight DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '损伤性重量(kg)',
    pharmaceutical_weight DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '药物性重量(kg)',
    chemical_weight DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '化学性重量(kg)',
    pathological_weight DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '病理性重量(kg)',
    total_weight DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总重量(kg)',
    record_date DATE NOT NULL COMMENT '录入日期',
    created_by VARCHAR(50) COMMENT '录入人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_batch_no (batch_no),
    INDEX idx_hospital_no (hospital_no),
    INDEX idx_region (region),
    INDEX idx_vehicle_no (vehicle_no),
    INDEX idx_staff_nos (staff_nos),
    INDEX idx_record_date (record_date),
    CONSTRAINT fk_record_hospital FOREIGN KEY (hospital_no) REFERENCES waste_hospital(hospital_no) ON DELETE RESTRICT,
    CONSTRAINT fk_record_vehicle FOREIGN KEY (vehicle_no) REFERENCES waste_vehicle(vehicle_no) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医废重量记录表';

-- 插入默认管理员账号（密码：admin123，使用BCrypt加密）
INSERT INTO sys_user (username, password, real_name, phone, email, role)
SELECT 'admin', '$2a$10$oNlJ5uqG3D0q1bACGGpItOV/Lj.QexJ7nLYMwuRRGOCRFmsHWIgSK', '系统管理员', '13800138000', 'admin@example.com', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');


