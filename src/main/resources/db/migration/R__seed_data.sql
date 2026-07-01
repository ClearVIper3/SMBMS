/*
=====================================================================================
 Flyway 可重复脚本 R__seed_data.sql
 - 仅在文件内容（checksum）变化时重跑；
 - 使用 INSERT ... ON DUPLICATE KEY UPDATE 保证幂等：
     新空库：插入种子数据；
     现网库（数据已存在）：更新非关键字段而不破坏关键关联（id 不变）；
 - 种子密码已用 BCrypt 预加密存储（不存明文）；
   现网库已有数据时，INSERT 会被 ON DUPLICATE KEY 拦截，
   不会覆盖已加密密码（更新列表里不包含 userPassword）。
=====================================================================================
*/

INSERT INTO `smbms_role`(`id`,`roleCode`,`roleName`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`) VALUES
(1,'R001','管理员',1,'2025-09-19 10:49:36',NULL,NULL),
(2,'R002','采购员',1,'2025-09-19 10:49:36',NULL,NULL),
(3,'R003','销售员',1,'2025-09-19 10:49:36',NULL,NULL)
ON DUPLICATE KEY UPDATE
    `roleName`     = VALUES(`roleName`),
    `creationDate` = VALUES(`creationDate`);

-- 种子密码已用 BCrypt 加密存储（原文见项目 README 初始密码说明）
INSERT INTO `smbms_user`(`id`,`userCode`,`userName`,`userPassword`,`gender`,`birthday`,`phone`,`address`,`userRole`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`) VALUES
(1,'U001','张三','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',1,'2025-09-27','13800000001','广州天河',2,1,'2025-09-19 10:49:36',NULL,NULL),
(2,'U002','李四','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',1,'2025-10-02','13800000002','深圳南山',2,1,'2025-09-19 10:49:36',NULL,NULL),
(3,'U003','王五','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',1,'2025-10-02','13800000003','北京朝阳',3,1,'2025-09-19 10:49:36',NULL,NULL),
(4,'admin','管理员','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',1,'2025-09-19','12345678901','中国',1,1,'2025-09-19 10:52:17',NULL,NULL)
ON DUPLICATE KEY UPDATE
    -- 注意：故意不更新 userPassword，保护已加密的现网密码不被明文覆盖
    `userName`     = VALUES(`userName`),
    `gender`       = VALUES(`gender`),
    `birthday`     = VALUES(`birthday`),
    `phone`        = VALUES(`phone`),
    `address`      = VALUES(`address`),
    `userRole`     = VALUES(`userRole`);

INSERT INTO `smbms_provider`(`id`,`proCode`,`proName`,`proDesc`,`proContact`,`proPhone`,`userAddress`,`userFax`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`) VALUES
(1,'P001','华南供应商','食品供应商','张经理','020-88888888','广州天河','020-88888889',1,'2025-09-19 10:49:36',NULL,NULL),
(2,'P002','北方供应商','电子产品供应商','李主管','010-66666666','北京朝阳','010-66666667',1,'2025-09-19 10:49:36',NULL,NULL),
(3,'P003','西南供应商','办公用品供应商','王主任','028-55555555','成都高新区','028-55555556',1,'2025-09-19 10:49:36',NULL,NULL)
ON DUPLICATE KEY UPDATE
    `proName`     = VALUES(`proName`),
    `proDesc`     = VALUES(`proDesc`),
    `proContact`  = VALUES(`proContact`),
    `proPhone`    = VALUES(`proPhone`),
    `userAddress` = VALUES(`userAddress`),
    `userFax`     = VALUES(`userFax`);

INSERT INTO `smbms_bill`(`id`,`billCode`,`productName`,`productDesc`,`productUnit`,`productCount`,`totalPrice`,`isPayment`,`providerId`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`) VALUES
(1,'B001','电脑',NULL,'台',2.00,12000.00,1,1,1,'2025-09-19 10:49:36',NULL,NULL),
(2,'B002','打印机',NULL,'台',5.00,2500.00,1,2,2,'2025-09-19 10:49:36',NULL,NULL),
(3,'B003','纸张',NULL,'包',10.00,500.00,2,3,1,'2025-09-19 10:49:36',NULL,NULL)
ON DUPLICATE KEY UPDATE
    `productName`  = VALUES(`productName`),
    `productUnit`  = VALUES(`productUnit`),
    `productCount` = VALUES(`productCount`),
    `totalPrice`   = VALUES(`totalPrice`),
    `isPayment`    = VALUES(`isPayment`),
    `providerId`   = VALUES(`providerId`);

INSERT INTO `smbms_address`(`id`,`contact`,`addressDesc`,`postCode`,`tel`,`userId`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`) VALUES
(1,'张三','广州天河区体育西路','510000','13800000001',1,1,'2025-09-19 10:49:36',NULL,NULL),
(2,'李四','深圳南山区科技园','518000','13800000002',2,1,'2025-09-19 10:49:36',NULL,NULL),
(3,'王五','北京朝阳区建国路','100020','13800000003',3,1,'2025-09-19 10:49:36',NULL,NULL)
ON DUPLICATE KEY UPDATE
    `contact`     = VALUES(`contact`),
    `addressDesc` = VALUES(`addressDesc`),
    `postCode`    = VALUES(`postCode`),
    `tel`         = VALUES(`tel`),
    `userId`      = VALUES(`userId`);
