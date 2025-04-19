package top.liuqiao.thumb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("top.liuqiao.thumb.mapper")
@EnableScheduling
public class ThumbSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThumbSystemApplication.class, args);
    }

}
