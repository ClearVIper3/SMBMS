/*
=====================================================================================
 V2: AI 助手相关表
   - ai_chat_session  会话
   - ai_chat_message  消息（含工具调用记录）

 设计要点：
   1. session.user_id  外键到 smbms_user(id)，ON DELETE CASCADE：用户删除时其会话一并清理；
   2. message.session_id 外键到 ai_chat_session(id)，ON DELETE CASCADE：会话删除时消息一并清理；
   3. role 字段约束：user / assistant / system / tool（与 LangChain4j ChatMessage 类型对齐）；
   4. tool_name / tool_arguments / tool_result：仅 role='tool' 或 assistant 触发工具时填写，
      便于前端展示"AI 调了哪个工具、传了什么、返回了什么"的 Tool-Call 气泡；
   5. content 用 mediumtext（最长 16MB），AI 长回复也能存下。
=====================================================================================
*/

CREATE TABLE IF NOT EXISTS `ai_chat_session` (
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     bigint       NOT NULL COMMENT '所属用户(smbms_user.id)',
    `title`       varchar(100) NOT NULL DEFAULT '新会话' COMMENT '会话标题(默认取首条用户消息)',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_ai_session_user` (`user_id`, `update_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 会话表';

CREATE TABLE IF NOT EXISTS `ai_chat_message` (
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id`     bigint       NOT NULL COMMENT '所属会话',
    `role`           varchar(16)  NOT NULL COMMENT 'user / assistant / system / tool',
    `content`        mediumtext            COMMENT '消息正文',
    `tool_name`      varchar(64)           COMMENT '工具名(role=tool 或 assistant 触发时填)',
    `tool_arguments` text                  COMMENT '工具入参 JSON',
    `tool_result`    mediumtext            COMMENT '工具返回 JSON',
    `tokens`         int                   COMMENT 'token 数(可选)',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_ai_msg_session` (`session_id`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 会话消息表';

/* 外键约束 —— 用与 V1 一致的存在性判断模式 */
DROP PROCEDURE IF EXISTS sp_add_ai_fk_if_absent;
DELIMITER //
CREATE PROCEDURE sp_add_ai_fk_if_absent(IN p_table VARCHAR(64), IN p_fk VARCHAR(64), IN p_ddl TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
         WHERE CONSTRAINT_SCHEMA = DATABASE()
           AND TABLE_NAME = p_table AND CONSTRAINT_NAME = p_fk AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    ) THEN
        SET @sql = p_ddl;
        PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL sp_add_ai_fk_if_absent('ai_chat_session', 'fk_ai_session_user',
    'ALTER TABLE `ai_chat_session` ADD CONSTRAINT `fk_ai_session_user` FOREIGN KEY (`user_id`) REFERENCES `smbms_user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE');

CALL sp_add_ai_fk_if_absent('ai_chat_message', 'fk_ai_msg_session',
    'ALTER TABLE `ai_chat_message` ADD CONSTRAINT `fk_ai_msg_session` FOREIGN KEY (`session_id`) REFERENCES `ai_chat_session`(`id`) ON DELETE CASCADE ON UPDATE CASCADE');

DROP PROCEDURE IF EXISTS sp_add_ai_fk_if_absent;
