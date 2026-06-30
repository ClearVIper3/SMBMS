-- 测试种子数据。密码字段写明文 '123456'，PasswordMigrationRunner 启动后会自动加密。
-- 数据量保持最小可验证集，避免拖慢测试。

INSERT INTO "smbms_role"("id","roleCode","roleName","createdBy","creationDate","modifyBy","modifyDate") VALUES
(1,'R001','管理员',1,'2025-09-19 10:49:36',NULL,NULL),
(2,'R002','采购员',1,'2025-09-19 10:49:36',NULL,NULL),
(3,'R003','销售员',1,'2025-09-19 10:49:36',NULL,NULL);

INSERT INTO "smbms_user"("id","userCode","userName","userPassword","gender","birthday","phone","address","userRole","createdBy","creationDate","modifyBy","modifyDate") VALUES
(1,'U001','张三','123456',1,'2025-09-27','13800000001','广州天河',2,1,'2025-09-19 10:49:36',NULL,NULL),
(2,'U002','李四','123456',1,'2025-10-02','13800000002','深圳南山',2,1,'2025-09-19 10:49:36',NULL,NULL),
(3,'U003','王五','123456',1,'2025-10-02','13800000003','北京朝阳',3,1,'2025-09-19 10:49:36',NULL,NULL),
(4,'admin','管理员','123456',1,'2025-09-19','12345678901','中国',1,1,'2025-09-19 10:52:17',NULL,NULL);

INSERT INTO "smbms_provider"("id","proCode","proName","proDesc","proContact","proPhone","userAddress","userFax","createdBy","creationDate","modifyBy","modifyDate") VALUES
(1,'P001','华南供应商','食品供应商','张经理','020-88888888','广州天河','020-88888889',1,'2025-09-19 10:49:36',NULL,NULL),
(2,'P002','北方供应商','电子产品供应商','李主管','010-66666666','北京朝阳','010-66666667',1,'2025-09-19 10:49:36',NULL,NULL),
(3,'P003','西南供应商','办公用品供应商','王主任','028-55555555','成都高新区','028-55555556',1,'2025-09-19 10:49:36',NULL,NULL);

INSERT INTO "smbms_bill"("id","billCode","productName","productDesc","productUnit","productCount","totalPrice","isPayment","providerId","createdBy","creationDate","modifyBy","modifyDate") VALUES
(1,'B001','电脑',NULL,'台',2.00,12000.00,1,1,1,'2025-09-19 10:49:36',NULL,NULL),
(2,'B002','打印机',NULL,'台',5.00,2500.00,1,2,2,'2025-09-19 10:49:36',NULL,NULL),
(3,'B003','纸张',NULL,'包',10.00,500.00,2,3,1,'2025-09-19 10:49:36',NULL,NULL);
