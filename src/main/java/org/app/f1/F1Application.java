package org.app.f1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class F1Application {

    public static void main(String[] args) {
        SpringApplication.run(F1Application.class, args);
    }
}
