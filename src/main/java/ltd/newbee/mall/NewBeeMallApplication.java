package ltd.newbee.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author 13
 * @qq交流群 796794009
 * @email 2449207463@qq.com
 * @link https://github.com/newbee-ltd
 */
@EnableAsync
@SpringBootApplication
public class NewBeeMallApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewBeeMallApplication.class, args);
    }
}
