package com.viper.config;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.viper.dao.user.UserMapper;
import com.viper.pojo.User;
import com.viper.utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 密码迁移器。
 * <p>
 * 应用启动时自动扫描所有用户，若发现仍为【明文】的密码（非 BCrypt 哈希格式），
 * 则将其加密后回写数据库。由此保证：即便初始化脚本中存在明文种子密码，
 * 应用启动后数据库内也不会再明文存储任何密码。
 */
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordMigrationRunner.class);

    private final UserMapper userMapper;

    public PasswordMigrationRunner(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public void run(String... args) {
        List<User> users = userMapper.selectList(null);
        if (users == null || users.isEmpty()) {
            return;
        }

        int migrated = 0;
        for (User user : users) {
            String pwd = user.getUserPassword();
            if (pwd != null && !pwd.isEmpty() && !PasswordUtil.isEncrypted(pwd)) {
                UpdateWrapper<User> wrapper = new UpdateWrapper<>();
                wrapper.eq("id", user.getId());
                wrapper.set("userPassword", PasswordUtil.encode(pwd));
                userMapper.update(null, wrapper);
                migrated++;
            }
        }

        if (migrated > 0) {
            log.info("密码迁移完成：已将 {} 条明文密码加密为 BCrypt 哈希。", migrated);
        }
    }
}
