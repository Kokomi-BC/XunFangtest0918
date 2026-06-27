package com.xunfang.manufacture.task;

import com.xunfang.manufacture.util.DMEUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * DME Token 自动刷新任务
 * 启动时立即执行一次，之后每 12 小时刷新一次
 *
 * @author xunfang
 */
@Component
public class DMETokenRefreshTask {

    private static final Logger logger = LoggerFactory.getLogger(DMETokenRefreshTask.class);

    @Autowired
    private DMEUtil dmeUtil;

    /**
     * 项目启动时立即刷新 Token，避免首次调用 401
     */
    @PostConstruct
    public void init() {
        try {
            logger.info("========== DME Token 初始化刷新 ==========");
            dmeUtil.getToken();
            logger.info("========== DME Token 初始化刷新完成 ==========");
        } catch (Exception e) {
            logger.error("DME Token 初始化刷新失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 每 12 小时刷新一次 Token（cron: 每天 0点、12点 执行）
     */
    // 注意：如需启用定时调度，取消下面注释并在启动类添加 @EnableScheduling
    // @Scheduled(cron = "0 0 0,12 * * ?")
    public void refreshToken() {
        try {
            logger.info("========== DME Token 定时刷新 ==========");
            dmeUtil.getToken();
            logger.info("========== DME Token 定时刷新完成 ==========");
        } catch (Exception e) {
            logger.error("DME Token 定时刷新失败: {}", e.getMessage(), e);
        }
    }
}
