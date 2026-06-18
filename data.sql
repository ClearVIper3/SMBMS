/*
=====================================================================================
 SMBMS 超市订单管理系统 - 数据库设计脚本（重构版）
 MySQL 8.0+ / InnoDB / utf8mb4
-------------------------------------------------------------------------------------
 设计要点：
   1. 每张表均有自增主键（PRIMARY KEY）。
   2. 业务编码列建立唯一约束（UNIQUE KEY），保证员工编码 / 角色编码 /
      供应商编码 / 订单编码不可重复。
   3. 建立真正的【数据库层外键约束（FOREIGN KEY）】，而非仅靠后端逻辑维护：
        - smbms_user.userRole   -> smbms_role(id)        用户所属角色
        - smbms_bill.providerId -> smbms_provider(id)    订单所属供应商
        - smbms_address.userId  -> smbms_user(id)        地址所属用户
        - 各表 createdBy / modifyBy -> smbms_user(id)     审计字段（创建人 / 修改人）
   4. 外键删除策略：
        - 角色被用户引用、供应商被订单引用 -> RESTRICT（禁止误删，保证引用完整性）
        - 用户地址 -> CASCADE（删除用户时级联删除其地址）
        - 审计字段 -> SET NULL（删除用户时，其创建/修改痕迹置空，不阻塞删除）
   5. 统一字符集 utf8mb4 / utf8mb4_unicode_ci，支持完整 Unicode（含 emoji）。
=====================================================================================
*/

/*!40101 SET NAMES utf8mb4 */;
/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE DATABASE /*!32312 IF NOT EXISTS*/`smbms` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `smbms`;

/* 先删除存在外键依赖的表（已关闭外键检查，顺序不影响，仅为可读性） */
DROP TABLE IF EXISTS `smbms_bill`;
DROP TABLE IF EXISTS `smbms_address`;
DROP TABLE IF EXISTS `smbms_user`;
DROP TABLE IF EXISTS `smbms_provider`;
DROP TABLE IF EXISTS `smbms_role`;

/* ------------------------------------------------------------------ */
/* 角色表 smbms_role                                                   */
/* ------------------------------------------------------------------ */
CREATE TABLE `smbms_role` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `roleCode`     varchar(30)  NOT NULL COMMENT '角色编码（唯一）',
    `roleName`     varchar(15)  NOT NULL COMMENT '角色名称',
    `createdBy`    bigint       DEFAULT NULL COMMENT '创建者(用户id)',
    `creationDate` datetime     DEFAULT NULL COMMENT '创建时间',
    `modifyBy`     bigint       DEFAULT NULL COMMENT '更新者(用户id)',
    `modifyDate`   datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_roleCode` (`roleCode`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

insert into `smbms_role`(`id`,`roleCode`,`roleName`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`) values
(1,'R001','管理员',1,'2025-09-19 10:49:36',NULL,NULL),
(2,'R002','采购员',1,'2025-09-19 10:49:36',NULL,NULL),
(3,'R003','销售员',1,'2025-09-19 10:49:36',NULL,NULL);

/* ------------------------------------------------------------------ */
/* 用户表 smbms_user                                                   */
/*   userRole  -> smbms_role(id)                                       */
/*   createdBy / modifyBy -> smbms_user(id) 自引用审计                  */
/* ------------------------------------------------------------------ */
CREATE TABLE `smbms_user` (
    `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `userCode`     varchar(15)  NOT NULL COMMENT '用户编码/员工唯一标识（唯一）',
    `userName`     varchar(15)  NOT NULL COMMENT '用户名字',
    `userPassword` varchar(20)  NOT NULL COMMENT '用户密码',
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

insert into `smbms_user`(`id`,`userCode`,`userName`,`userPassword`,`gender`,`birthday`,`phone`,`address`,`userRole`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`) values
(1,'U001','张三','123456',1,'2025-09-27','13800000001','广州天河',2,1,'2025-09-19 10:49:36',4,'2025-09-27 17:34:06'),
(2,'U002','李四','123456',1,'2025-10-02','13800000002','深圳南山',2,1,'2025-09-19 10:49:36',4,'2025-10-02 17:43:13'),
(3,'U003','王五','123456',1,'2025-10-02','13800000003','北京朝阳',3,1,'2025-09-19 10:49:36',4,'2025-10-02 17:42:15'),
(4,'admin','管理员','123456',1,'2025-09-19','12345678901','中国',1,1,'2025-09-19 10:52:17',NULL,NULL);

/* ------------------------------------------------------------------ */
/* 供应商表 smbms_provider                                             */
/* ------------------------------------------------------------------ */
CREATE TABLE `smbms_provider` (
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商表';

insert into `smbms_provider`(`id`,`proCode`,`proName`,`proDesc`,`proContact`,`proPhone`,`userAddress`,`userFax`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`) values
(1,'P001','华南供应商','食品供应商','张经理','020-88888888','广州天河','020-88888889',1,'2025-09-19 10:49:36',NULL,NULL),
(2,'P002','北方供应商','电子产品供应商','李主管','010-66666666','北京朝阳','010-66666667',1,'2025-09-19 10:49:36',NULL,NULL),
(3,'P003','西南供应商','办公用品供应商','王主任','028-55555555','成都高新区','028-55555556',1,'2025-09-19 10:49:36',NULL,NULL);

/* ------------------------------------------------------------------ */
/* 订单表 smbms_bill                                                   */
/*   providerId -> smbms_provider(id)                                  */
/* ------------------------------------------------------------------ */
CREATE TABLE `smbms_bill` (
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

insert into `smbms_bill`(`id`,`billCode`,`productName`,`productDesc`,`productUnit`,`productCount`,`totalPrice`,`isPayment`,`providerId`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`) values
(1,'B001','电脑',NULL,'台',2.00,12000.00,1,1,1,'2025-09-19 10:49:36',4,'2025-11-17 04:16:43'),
(2,'B002','打印机',NULL,'台',5.00,2500.00,1,2,2,'2025-09-19 10:49:36',4,'2025-11-17 04:16:37'),
(3,'B003','纸张',NULL,'包',10.00,500.00,2,3,1,'2025-09-19 10:49:36',4,'2025-11-11 08:01:42');

/* ------------------------------------------------------------------ */
/* 地址表 smbms_address                                                */
/*   userId -> smbms_user(id)                                          */
/* ------------------------------------------------------------------ */
CREATE TABLE `smbms_address` (
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地址表';

insert into `smbms_address`(`id`,`contact`,`addressDesc`,`postCode`,`tel`,`userId`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`) values
(1,'张三','广州天河区体育西路','510000','13800000001',1,1,'2025-09-19 10:49:36',NULL,NULL),
(2,'李四','深圳南山区科技园','518000','13800000002',2,1,'2025-09-19 10:49:36',NULL,NULL),
(3,'王五','北京朝阳区建国路','100020','13800000003',3,1,'2025-09-19 10:49:36',NULL,NULL);

/* ================================================================== */
/* 添加外键约束（数据插入完成后统一添加，逻辑更清晰）                   */
/* ================================================================== */

/* 用户 -> 角色；用户审计字段 -> 用户（自引用） */
ALTER TABLE `smbms_user`
    ADD CONSTRAINT `fk_user_role`      FOREIGN KEY (`userRole`)  REFERENCES `smbms_role`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_user_createdBy` FOREIGN KEY (`createdBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_user_modifyBy`  FOREIGN KEY (`modifyBy`)  REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

/* 角色审计字段 -> 用户 */
ALTER TABLE `smbms_role`
    ADD CONSTRAINT `fk_role_createdBy` FOREIGN KEY (`createdBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_role_modifyBy`  FOREIGN KEY (`modifyBy`)  REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

/* 供应商审计字段 -> 用户 */
ALTER TABLE `smbms_provider`
    ADD CONSTRAINT `fk_provider_createdBy` FOREIGN KEY (`createdBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_provider_modifyBy`  FOREIGN KEY (`modifyBy`)  REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

/* 订单 -> 供应商；订单审计字段 -> 用户 */
ALTER TABLE `smbms_bill`
    ADD CONSTRAINT `fk_bill_provider`  FOREIGN KEY (`providerId`) REFERENCES `smbms_provider`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_bill_createdBy` FOREIGN KEY (`createdBy`)  REFERENCES `smbms_user`(`id`)     ON DELETE SET NULL ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_bill_modifyBy`  FOREIGN KEY (`modifyBy`)   REFERENCES `smbms_user`(`id`)     ON DELETE SET NULL ON UPDATE CASCADE;

/* 地址 -> 用户（级联删除）；地址审计字段 -> 用户 */
ALTER TABLE `smbms_address`
    ADD CONSTRAINT `fk_address_user`      FOREIGN KEY (`userId`)    REFERENCES `smbms_user`(`id`) ON DELETE CASCADE  ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_address_createdBy` FOREIGN KEY (`createdBy`) REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_address_modifyBy`  FOREIGN KEY (`modifyBy`)  REFERENCES `smbms_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
