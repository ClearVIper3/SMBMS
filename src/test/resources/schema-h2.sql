-- 测试期 H2 建表脚本。
-- 与 db/migration/V1__init_schema.sql + V2__ai_chat_tables.sql 的结构对齐，
-- 去掉 MySQL 专属语法（ENGINE / CHARSET / COMMENT / DELIMITER 等）。

DROP TABLE IF EXISTS "ai_chat_message";
DROP TABLE IF EXISTS "ai_chat_session";
DROP TABLE IF EXISTS "smbms_address";
DROP TABLE IF EXISTS "smbms_bill";
DROP TABLE IF EXISTS "smbms_user";
DROP TABLE IF EXISTS "smbms_provider";
DROP TABLE IF EXISTS "smbms_role";

CREATE TABLE "smbms_role" (
    "id"           BIGINT       NOT NULL AUTO_INCREMENT,
    "roleCode"     VARCHAR(30)  NOT NULL,
    "roleName"     VARCHAR(15)  NOT NULL,
    "createdBy"    BIGINT       DEFAULT NULL,
    "creationDate" TIMESTAMP    DEFAULT NULL,
    "modifyBy"     BIGINT       DEFAULT NULL,
    "modifyDate"   TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_role_roleCode" UNIQUE ("roleCode")
);

CREATE TABLE "smbms_user" (
    "id"           BIGINT       NOT NULL AUTO_INCREMENT,
    "userCode"     VARCHAR(15)  NOT NULL,
    "userName"     VARCHAR(15)  NOT NULL,
    "userPassword" VARCHAR(100) NOT NULL,
    "gender"       INT          DEFAULT NULL,
    "birthday"     DATE         DEFAULT NULL,
    "phone"        VARCHAR(20)  DEFAULT NULL,
    "address"      VARCHAR(30)  DEFAULT NULL,
    "userRole"     BIGINT       DEFAULT NULL,
    "createdBy"    BIGINT       DEFAULT NULL,
    "creationDate" TIMESTAMP    DEFAULT NULL,
    "modifyBy"     BIGINT       DEFAULT NULL,
    "modifyDate"   TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_user_userCode" UNIQUE ("userCode")
);

CREATE TABLE "smbms_provider" (
    "id"           BIGINT       NOT NULL AUTO_INCREMENT,
    "proCode"      VARCHAR(15)  NOT NULL,
    "proName"      VARCHAR(15)  NOT NULL,
    "proDesc"      VARCHAR(50)  DEFAULT NULL,
    "proContact"   VARCHAR(15)  DEFAULT NULL,
    "proPhone"     VARCHAR(20)  DEFAULT NULL,
    "userAddress"  VARCHAR(30)  DEFAULT NULL,
    "userFax"      VARCHAR(20)  DEFAULT NULL,
    "createdBy"    BIGINT       DEFAULT NULL,
    "creationDate" TIMESTAMP    DEFAULT NULL,
    "modifyBy"     BIGINT       DEFAULT NULL,
    "modifyDate"   TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_provider_proCode" UNIQUE ("proCode")
);

CREATE TABLE "smbms_bill" (
    "id"           BIGINT        NOT NULL AUTO_INCREMENT,
    "billCode"     VARCHAR(30)   NOT NULL,
    "productName"  VARCHAR(20)   DEFAULT NULL,
    "productDesc"  VARCHAR(50)   DEFAULT NULL,
    "productUnit"  VARCHAR(60)   DEFAULT NULL,
    "productCount" DECIMAL(20,2) DEFAULT NULL,
    "totalPrice"   DECIMAL(20,2) DEFAULT NULL,
    "isPayment"    INT           DEFAULT NULL,
    "providerId"   BIGINT        DEFAULT NULL,
    "createdBy"    BIGINT        DEFAULT NULL,
    "creationDate" TIMESTAMP     DEFAULT NULL,
    "modifyBy"     BIGINT        DEFAULT NULL,
    "modifyDate"   TIMESTAMP     DEFAULT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "uk_bill_billCode" UNIQUE ("billCode")
);

CREATE TABLE "smbms_address" (
    "id"           BIGINT       NOT NULL AUTO_INCREMENT,
    "contact"      VARCHAR(15)  DEFAULT NULL,
    "addressDesc"  VARCHAR(50)  DEFAULT NULL,
    "postCode"     VARCHAR(15)  DEFAULT NULL,
    "tel"          VARCHAR(20)  DEFAULT NULL,
    "userId"       BIGINT       DEFAULT NULL,
    "createdBy"    BIGINT       DEFAULT NULL,
    "creationDate" TIMESTAMP    DEFAULT NULL,
    "modifyBy"     BIGINT       DEFAULT NULL,
    "modifyDate"   TIMESTAMP    DEFAULT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE "ai_chat_session" (
    "id"          BIGINT       NOT NULL AUTO_INCREMENT,
    "user_id"     BIGINT       NOT NULL,
    "title"       VARCHAR(100) NOT NULL DEFAULT '新会话',
    "create_time" TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "update_time" TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);

CREATE TABLE "ai_chat_message" (
    "id"             BIGINT       NOT NULL AUTO_INCREMENT,
    "session_id"     BIGINT       NOT NULL,
    "role"           VARCHAR(16)  NOT NULL,
    "content"        CLOB,
    "tool_name"      VARCHAR(64),
    "tool_arguments" CLOB,
    "tool_result"    CLOB,
    "tokens"         INT,
    "create_time"    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("id")
);

ALTER TABLE "smbms_user"      ADD CONSTRAINT "fk_user_role"    FOREIGN KEY ("userRole")   REFERENCES "smbms_role"("id");
ALTER TABLE "smbms_bill"      ADD CONSTRAINT "fk_bill_provider"FOREIGN KEY ("providerId") REFERENCES "smbms_provider"("id");
ALTER TABLE "smbms_address"   ADD CONSTRAINT "fk_address_user" FOREIGN KEY ("userId")     REFERENCES "smbms_user"("id");
ALTER TABLE "ai_chat_session" ADD CONSTRAINT "fk_ai_session_user" FOREIGN KEY ("user_id") REFERENCES "smbms_user"("id");
ALTER TABLE "ai_chat_message" ADD CONSTRAINT "fk_ai_msg_session"  FOREIGN KEY ("session_id") REFERENCES "ai_chat_session"("id");
