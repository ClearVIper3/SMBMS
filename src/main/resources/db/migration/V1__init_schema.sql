/*
=====================================================================================
 Flyway 基线脚本 V1：SMBMS 初始 schema（MySQL 8 / InnoDB / utf8mb4）

 设计要点：
   1. 全部使用 CREATE TABLE IF NOT EXISTS，幂等，防止误删现网数据；
   2. 不在 V1 中写 INSERT 种子数据 —— 种子数据放到 R__seed_data.sql（幂等可重复）；
   3. 外键约束在表创建之后统一 ADD，并通过存储过程检查避免重复添加。

 平滑接入说明（现网已有数据库时）：
   * application.yaml 中 flyway.baseline-on-migrate=true + baseline-version=1
     会让 Flyway 检测到非空库后，**不执行本脚本**，仅把当前状态标记为 V1，
     从下一个版本 V2 开始迁移。详见 MIGRATION_DB.md。
=====================================================================================
*/

/* ------------------------------------------------------------------ */
/* 角色表 smbms_role                                                   */
/* ------------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS `smbms_role` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `roleCode`     varchar(30)  NOT NULL COMMENT '角色编码（唯一）',
    `roleName`     varchar(15)  NOT NULL COMMENT '角色名称',
    `createdBy`    bigint       DEFAULT NULL COMMENT '创建者(用户id)',
    `creationDate` datetime     DEFAULT NULL COMMENT '创建时间',
    `modifyBy`     bigint       DEFAULT NULL COMMENT '更新者(用户id)',
    `modifyDate`   datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_roleCode` (`roleCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

/* ------------------------------------------------------------------ */
/* 用户表 smbms_user                                                   */
/* ------------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS `smbms_user` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `userCode`     varchar(15)  NOT NULL COMMENT '用户编码/员工唯一标识（唯一）',
    `userName`     varchar(15)  NOT NULL COMMENT '用户名字',
    `userPassword` varchar(100) NOT NULL COMMENT '用户密码(BCrypt加密哈希，非明文)',
    `gender`       int          DEFAULT NULL COMMENT '性别(1:男 2:女)',
    `birthday`     date         DEFAULT NULL COMMENT '出生日期',
    `phone`        varchar(20)  DEFAULT NULL COMMENT '电话',
    `address`      varchar(30)  DEFAULT NULL COMMENT '地址',
    `userRole`     bigint       DEFAULT NULL COMMENT '用户角色(关联smbms_role.id)',
    `createdBy`    bigint       DEFAULT NULL COMMENT '创建者(用户id)',
    `creationDate` datetime     DEFAULT NULL COMMENT '创建时间',
    `modifyBy`     bigint       DEFAULT NULL COMMENT '更新者(用户id)',
    `modifyDate`   datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_userCode` (`userCode`),
    KEY `idx_user_userRole` (`userRole`),
    KEY `idx_user_createdBy` (`createdBy`),
    KEY `idx_user_modifyBy` (`modifyBy`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

/* ------------------------------------------------------------------ */
/* 供应商表 smbms_provider                                             */
/* ------------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS `smbms_provider` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `proCode`      varchar(15)  NOT NULL COMMENT '供应商编码（唯一）',
    `proName`      varchar(15)  NOT NULL COMMENT '供应商名称',
    `proDesc`      varchar(50)  DEFAULT NULL COMMENT '供应商描述',
    `proContact`   varchar(15)  DEFAULT NULL COMMENT '供应商联系人',
    `proPhone`     varchar(20)  DEFAULT NULL COMMENT '供应商电话',
    `userAddress`  varchar(30)  DEFAULT NULL COMMENT '供应商地址',
    `userFax`      varchar(20)  DEFAULT NULL COMMENT '供应商传真',
    `createdBy`    bigint       DEFAULT NULL COMMENT '创建者(用户id)',
    `creationDate` datetime     DEFAULT NULL COMMENT '创建时间',
    `modifyBy`     bigint       DEFAULT NULL COMMENT '更新者(用户id)',
    `modifyDate`   datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_provider_proCode` (`proCode`),
    KEY `idx_provider_createdBy` (`createdBy`),
    KEY `idx_provider_modifyBy` (`modifyBy`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商表';

/* ------------------------------------------------------------------ */
/* 订单表 smbms_bill                                                   */
/* ------------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS `smbms_bill` (
    `id`           bigint        NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `billCode`     varchar(30)   NOT NULL COMMENT '账单编码（唯一）',
    `productName`  varchar(20)   DEFAULT NULL COMMENT '商品名称',
    `productDesc`  varchar(50)   DEFAULT NULL COMMENT '商品描述',
    `productUnit`  varchar(60)   DEFAULT NULL COMMENT '商品单位',
    `productCount` decimal(20,2) DEFAULT NULL COMMENT '商品数量',
    `totalPrice`   decimal(20,2) DEFAULT NULL COMMENT '总金额',
    `isPayment`    int           DEFAULT NULL COMMENT '是否支付(1:未付款 2:已付款)',
    `providerId`   bigint        DEFAULT NULL COMMENT '供应商id(关联smbms_provider.id)',
    `createdBy`    bigint        DEFAULT NULL COMMENT '创建者(用户id)',
    `creationDate` datetime      DEFAULT NULL COMMENT '创建时间',
    `modifyBy`     bigint        DEFAULT NULL COMMENT '更新者(用户id)',
    `modifyDate`   datetime      DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bill_billCode` (`billCode`),
    KEY `idx_bill_providerId` (`providerId`),
    KEY `idx_bill_createdBy` (`createdBy`),
    KEY `idx_bill_modifyBy` (`modifyBy`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

/* ------------------------------------------------------------------ */
/* 地址表 smbms_address                                                */
/* ------------------------------------------------------------------ */
CREATE TABLE IF NOT EXISTS `smbms_address` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `contact`      varchar(15)  DEFAULT NULL COMMENT '联系人',
    `addressDesc`  varchar(50)  DEFAULT NULL COMMENT '地址描述',
    `postCode`     varchar(15)  DEFAULT NULL COMMENT '邮编',
    `tel`          varchar(20)  DEFAULT NULL COMMENT '联系电话',
    `userId`       bigint       DEFAULT NULL COMMENT '所属用户id(关联smbms_user.id)',
    `createdBy`    bigint       DEFAULT NULL COMMENT '创建者(用户id)',
    `creationDate` datetime     DEFAULT NULL COMMENT '创建时间',
    `modifyBy`     bigint       DEFAULT NULL COMMENT '更新者(用户id)',
    `modifyDate`   datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_address_userId` (`userId`),
    KEY `idx_address_createdBy` (`createdBy`),
    KEY `idx_address_modifyBy` (`modifyBy`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地址表';

/* ================================================================== */
/* 外键约束统一添加。                                                  */
/* 使用 INFORMATION_SCHEMA 判断是否已存在，避免 ALTER 时重复添加报错。  */
/* （MySQL 8 不支持 ADD CONSTRAINT IF NOT EXISTS，此处用动态 SQL 包裹） */
/* ================================================================== */

/* 通用存储过程：仅当外键不存在时才添加 */
DROP PROCEDURE IF EXISTS sp_add_fk_if_absent;
DELIMITER //
CREATE PROCEDURE sp_add_fk_if_absent(
    IN p_table  VARCHAR(64),
    IN p_fkName VARCHAR(64),
    IN p_ddl    TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
         WHERE CONSTRAINT_SCHEMA = DATABASE()
           AND TABLE_NAME        = p_table
           AND CONSTRAINT_NAME   = p_fkName
           AND CONSTRAINT_TYPE   = 'FOREIGN KEY'
    ) THEN
        SET @sql = p_ddl;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL sp_add_fk_if_absent('smbms_user', 'fk_user_role',
    'ALTER TABLE `smbms_user` ADD CONSTRAINT `fk_user_role` FOREIGN KEY (`userRole`) REFERENCES `smbms_role`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE');
CALL sp_add_fk_if_absent('smbms_user', 'fk_user_createdBy',
    'ALTER TABLE `smbms_user` ADD CONSTRAINT `fk_user_createdBy` FOREIGN KEY (`createdBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE');
CALL sp_add_fk_if_absent('smbms_user', 'fk_user_modifyBy',
    'ALTER TABLE `smbms_user` ADD CONSTRAINT `fk_user_modifyBy` FOREIGN KEY (`modifyBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE');

CALL sp_add_fk_if_absent('smbms_role', 'fk_role_createdBy',
    'ALTER TABLE `smbms_role` ADD CONSTRAINT `fk_role_createdBy` FOREIGN KEY (`createdBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE');
CALL sp_add_fk_if_absent('smbms_role', 'fk_role_modifyBy',
    'ALTER TABLE `smbms_role` ADD CONSTRAINT `fk_role_modifyBy` FOREIGN KEY (`modifyBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE');

CALL sp_add_fk_if_absent('smbms_provider', 'fk_provider_createdBy',
    'ALTER TABLE `smbms_provider` ADD CONSTRAINT `fk_provider_createdBy` FOREIGN KEY (`createdBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE');
CALL sp_add_fk_if_absent('smbms_provider', 'fk_provider_modifyBy',
    'ALTER TABLE `smbms_provider` ADD CONSTRAINT `fk_provider_modifyBy` FOREIGN KEY (`modifyBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE');

CALL sp_add_fk_if_absent('smbms_bill', 'fk_bill_provider',
    'ALTER TABLE `smbms_bill` ADD CONSTRAINT `fk_bill_provider` FOREIGN KEY (`providerId`) REFERENCES `smbms_provider`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE');
CALL sp_add_fk_if_absent('smbms_bill', 'fk_bill_createdBy',
    'ALTER TABLE `smbms_bill` ADD CONSTRAINT `fk_bill_createdBy` FOREIGN KEY (`createdBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE');
CALL sp_add_fk_if_absent('smbms_bill', 'fk_bill_modifyBy',
    'ALTER TABLE `smbms_bill` ADD CONSTRAINT `fk_bill_modifyBy` FOREIGN KEY (`modifyBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE');

CALL sp_add_fk_if_absent('smbms_address', 'fk_address_user',
    'ALTER TABLE `smbms_address` ADD CONSTRAINT `fk_address_user` FOREIGN KEY (`userId`) REFERENCES `smbms_user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE');
CALL sp_add_fk_if_absent('smbms_address', 'fk_address_createdBy',
    'ALTER TABLE `smbms_address` ADD CONSTRAINT `fk_address_createdBy` FOREIGN KEY (`createdBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE');
CALL sp_add_fk_if_absent('smbms_address', 'fk_address_modifyBy',
    'ALTER TABLE `smbms_address` ADD CONSTRAINT `fk_address_modifyBy` FOREIGN KEY (`modifyBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE');

DROP PROCEDURE IF EXISTS sp_add_fk_if_absent;
