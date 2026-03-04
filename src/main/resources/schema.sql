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

-- 插入默认管理员账号（密码：admin123，使用BCrypt加密）
INSERT INTO sys_user (username, password, real_name, phone, email, role)
SELECT 'admin', '$2a$10$oNlJ5uqG3D0q1bACGGpItOV/Lj.QexJ7nLYMwuRRGOCRFmsHWIgSK', '系统管理员', '13800138000', 'admin@example.com', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

-- 插入测试机构数据
INSERT INTO institution (institution_no, institution_name, institution_type, contact_person, contact_phone, contact_email, region, address, institution_status, remark) VALUES
('100001', '忻府区人民医院', 'HOSPITAL', '张三', '13800138001', 'zhangsan@hospital.com', '忻府区', '忻府区和平街1号', 'NORMAL', '二级甲等医院'),
('100002', '原平市中医院', 'HOSPITAL', '李四', '13800138002', 'lisi@hospital.com', '原平市', '原平市建设路2号', 'NORMAL', '二级甲等医院'),
('100003', '定襄县卫生院', 'HEALTH_CENTER', '王五', '13800138003', 'wangwu@health.com', '定襄县', '定襄县解放路3号', 'NORMAL', '乡镇卫生院'),
('100004', '五台县卫生所', 'HEALTH_STATION', '赵六', '13800138004', 'zhaoliu@health.com', '五台县', '五台县台城镇4号', 'NORMAL', '村级卫生所'),
('100005', '代县社区卫生服务中心', 'COMMUNITY', '孙七', '13800138005', 'sunqi@community.com', '代县', '代县上馆镇5号', 'NORMAL', '社区卫生服务中心'),
('100006', '繁峙县诊所', 'CLINIC', '周八', '13800138006', 'zhouba@clinic.com', '繁峙县', '繁峙县繁城镇6号', 'PAUSED', '私立诊所'),
('100007', '静乐县药店', 'PHARMACY', '吴九', '13800138007', 'wujiu@pharmacy.com', '静乐县', '静乐县鹅城镇7号', 'NORMAL', '连锁药店'),
('100008', '宁武县检验中心', 'LABORATORY', '郑十', '13800138008', 'zhengshi@lab.com', '宁武县', '宁武县凤凰镇8号', 'NORMAL', '医学检验中心'),
('100009', '神池县卫生院', 'HEALTH_CENTER', '钱十一', '13800138009', 'qianshiyi@health.com', '神池县', '神池县龙泉镇9号', 'NORMAL', '乡镇卫生院'),
('100010', '五寨县卫生所', 'HEALTH_STATION', '王十二', '13800138010', 'wangshier@health.com', '五寨县', '五寨县砚城镇10号', 'NORMAL', '村级卫生所'),
('100011', '岢岚县人民医院', 'HOSPITAL', '李十三', '13800138011', 'lishisan@hospital.com', '岢岚县', '岢岚县岚漪镇11号', 'NORMAL', '二级乙等医院'),
('100012', '保德县中医院', 'HOSPITAL', '张十四', '13800138012', 'zhangshisi@hospital.com', '保德县', '保德县东关镇12号', 'NORMAL', '二级甲等医院'),
('100013', '河曲县社区卫生服务中心', 'COMMUNITY', '刘十五', '13800138013', 'liushiwu@community.com', '河曲县', '河曲县文笔镇13号', 'NORMAL', '社区卫生服务中心'),
('100014', '偏关县诊所', 'CLINIC', '陈十六', '13800138014', 'chenshiliu@clinic.com', '偏关县', '偏关县新关镇14号', 'NORMAL', '私立诊所')
ON DUPLICATE KEY UPDATE institution_name = VALUES(institution_name);
