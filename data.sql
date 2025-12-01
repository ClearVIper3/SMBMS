/*
SQLyog Community v13.3.0 (64 bit)
MySQL - 8.0.41 : Database - smbms
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`smbms` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `smbms`;

/*Table structure for table `smbms_address` */

DROP TABLE IF EXISTS `smbms_address`;

CREATE TABLE `smbms_address` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `contact` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                 `addressDesc` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                 `postCode` varchar(15) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                 `tel` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                 `createdBy` bigint DEFAULT NULL,
                                 `creationDate` datetime DEFAULT NULL,
                                 `modifyBy` bigint DEFAULT NULL,
                                 `modifyDate` datetime DEFAULT NULL,
                                 `userId` bigint DEFAULT NULL,
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Data for the table `smbms_address` */

insert  into `smbms_address`(`id`,`contact`,`addressDesc`,`postCode`,`tel`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`,`userId`) values
                                                                                                                                            (1,'张三','广州天河区体育西路','510000','13800000001',1,'2025-09-19 10:49:36',NULL,NULL,1),
                                                                                                                                            (2,'李四','深圳南山区科技园','518000','13800000002',1,'2025-09-19 10:49:36',NULL,NULL,2),
                                                                                                                                            (3,'王五','北京朝阳区建国路','100020','13800000003',1,'2025-09-19 10:49:36',NULL,NULL,3);

/*Table structure for table `smbms_bill` */

DROP TABLE IF EXISTS `smbms_bill`;

CREATE TABLE `smbms_bill` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                              `billCode` varchar(30) DEFAULT NULL COMMENT '账单编码',
                              `productName` varchar(20) DEFAULT NULL COMMENT '商品名称',
                              `productDesc` varchar(50) DEFAULT NULL COMMENT '商品描述',
                              `productUnit` varchar(60) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL COMMENT '商品单位',
                              `productCount` decimal(20,2) DEFAULT NULL COMMENT '商品数量',
                              `totalPrice` decimal(20,2) DEFAULT NULL COMMENT '总金额',
                              `isPayment` int DEFAULT NULL COMMENT '是否支付',
                              `createdBy` bigint DEFAULT NULL COMMENT '创建者',
                              `creationDate` datetime DEFAULT NULL COMMENT '创建时间',
                              `modifyBy` bigint DEFAULT NULL COMMENT '更新者',
                              `modifyDate` datetime DEFAULT NULL COMMENT '更新时间',
                              `providerId` bigint DEFAULT NULL COMMENT '供应商id',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb3;

/*Data for the table `smbms_bill` */

insert  into `smbms_bill`(`id`,`billCode`,`productName`,`productDesc`,`productUnit`,`productCount`,`totalPrice`,`isPayment`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`,`providerId`) values
                                                                                                                                                                                                 (1,'B001','电脑',NULL,'台',2.00,12000.00,1,1,'2025-09-19 10:49:36',4,'2025-11-17 04:16:43',1),
                                                                                                                                                                                                 (2,'B002','打印机',NULL,'台',5.00,2500.00,1,2,'2025-09-19 10:49:36',4,'2025-11-17 04:16:37',2),
                                                                                                                                                                                                 (3,'B003','纸张',NULL,'包',10.00,500.00,2,1,'2025-09-19 10:49:36',4,'2025-11-11 08:01:42',3);

/*Table structure for table `smbms_provider` */

DROP TABLE IF EXISTS `smbms_provider`;

CREATE TABLE `smbms_provider` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                                  `proCode` varchar(15) DEFAULT NULL COMMENT '供应商编码',
                                  `proName` varchar(15) DEFAULT NULL COMMENT '供应商名称',
                                  `proDesc` varchar(50) DEFAULT NULL COMMENT '供应商描述',
                                  `proContact` varchar(15) DEFAULT NULL COMMENT '供应商联系人',
                                  `proPhone` varchar(20) DEFAULT NULL COMMENT '供应商电话',
                                  `userAddress` varchar(30) DEFAULT NULL COMMENT '供应商地址',
                                  `userFax` varchar(20) DEFAULT NULL COMMENT '供应商传真',
                                  `createdBy` bigint DEFAULT NULL COMMENT '创建者',
                                  `creationDate` datetime DEFAULT NULL COMMENT '创建时间',
                                  `modifyBy` bigint DEFAULT NULL COMMENT '更新者',
                                  `modifyDate` datetime DEFAULT NULL COMMENT '更新时间',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=417726466 DEFAULT CHARSET=utf8mb3;

/*Data for the table `smbms_provider` */

insert  into `smbms_provider`(`id`,`proCode`,`proName`,`proDesc`,`proContact`,`proPhone`,`userAddress`,`userFax`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`) values
                                                                                                                                                                         (1,'P001','华南供应商','食品供应商','张经理','020-88888888','广州天河','020-88888889',1,'2025-09-19 10:49:36',NULL,NULL),
                                                                                                                                                                         (2,'P002','北方供应商','电子产品供应商','李主管','010-66666666','北京朝阳','010-66666667',1,'2025-09-19 10:49:36',NULL,NULL),
                                                                                                                                                                         (3,'P003','西南供应商','办公用品供应商','王主任','028-55555555','成都高新区','028-55555556',1,'2025-09-19 10:49:36',NULL,NULL);

/*Table structure for table `smbms_role` */

DROP TABLE IF EXISTS `smbms_role`;

CREATE TABLE `smbms_role` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                              `roleCode` varchar(30) DEFAULT NULL COMMENT '角色编码',
                              `roleName` varchar(15) DEFAULT NULL COMMENT '角色名称',
                              `createdBy` bigint DEFAULT NULL COMMENT '创建者',
                              `creationDate` datetime DEFAULT NULL COMMENT '创建时间',
                              `modifyBy` bigint DEFAULT NULL COMMENT '更新者',
                              `modifyDate` datetime DEFAULT NULL COMMENT '更新时间',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3;

/*Data for the table `smbms_role` */

insert  into `smbms_role`(`id`,`roleCode`,`roleName`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`) values
                                                                                                             (1,'R001','管理员',1,'2025-09-19 10:49:36',NULL,NULL),
                                                                                                             (2,'R002','采购员',1,'2025-09-19 10:49:36',NULL,NULL),
                                                                                                             (3,'R003','销售员',1,'2025-09-19 10:49:36',NULL,NULL);

/*Table structure for table `smbms_user` */

DROP TABLE IF EXISTS `smbms_user`;

CREATE TABLE `smbms_user` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                              `userCode` varchar(15) DEFAULT NULL COMMENT '用户编码',
                              `userName` varchar(15) DEFAULT NULL COMMENT '用户名字',
                              `userPassword` varchar(20) DEFAULT NULL COMMENT '用户密码',
                              `gender` int DEFAULT NULL COMMENT '性别',
                              `birthday` date DEFAULT NULL COMMENT '出生日期',
                              `phone` varchar(20) DEFAULT NULL COMMENT '电话',
                              `address` varchar(30) DEFAULT NULL COMMENT '地址',
                              `userRole` bigint DEFAULT NULL COMMENT '用户角色',
                              `createdBy` bigint DEFAULT NULL COMMENT '创建者',
                              `creationDate` datetime DEFAULT NULL COMMENT '创建时间',
                              `modifyBy` bigint DEFAULT NULL COMMENT '更新者',
                              `modifyDate` datetime DEFAULT NULL COMMENT '更新时间',
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1948696579 DEFAULT CHARSET=utf8mb3;

/*Data for the table `smbms_user` */

insert  into `smbms_user`(`id`,`userCode`,`userName`,`userPassword`,`gender`,`birthday`,`phone`,`address`,`userRole`,`createdBy`,`creationDate`,`modifyBy`,`modifyDate`) values
                                                                                                                                                                             (1,'U001','张三','123456',1,'2025-09-27','13800000001','广州天河',2,1,'2025-09-19 10:49:36',4,'2025-09-27 17:34:06'),
                                                                                                                                                                             (2,'U002','李四','123456',1,'2025-10-02','13800000002','深圳南山',2,1,'2025-09-19 10:49:36',4,'2025-10-02 17:43:13'),
                                                                                                                                                                             (3,'U003','王五','123456',1,'2025-10-02','13800000003','北京朝阳',3,1,'2025-09-19 10:49:36',4,'2025-10-02 17:42:15'),
                                                                                                                                                                             (4,'admin','管理员','123456',1,'2025-09-19','12345678901','中国',1,1,'2025-09-19 10:52:17',NULL,NULL);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
