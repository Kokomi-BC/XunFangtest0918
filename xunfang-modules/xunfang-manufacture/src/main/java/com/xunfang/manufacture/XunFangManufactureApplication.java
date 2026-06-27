package com.xunfang.manufacture;

import com.xunfang.common.security.annotation.EnableCustomConfig;
import com.xunfang.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 制造模块（供应商管理 / 采购订单管理）
 *
 * @author xunfang
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class XunFangManufactureApplication {

    public static void main(String[] args) {
        SpringApplication.run(XunFangManufactureApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  制造模块启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      \\           \n" +
                " ''-'   `'-'    `-..-'              ");
    }
}
