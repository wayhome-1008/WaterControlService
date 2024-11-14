package com.zjtc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Way
 */
@SpringBootApplication
@EnableScheduling
public class WaterControlServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WaterControlServiceApplication.class, args);
    }

}
