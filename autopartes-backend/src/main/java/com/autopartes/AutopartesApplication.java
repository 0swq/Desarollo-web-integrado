package com.autopartes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AutopartesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutopartesApplication.class, args);
    }
}
