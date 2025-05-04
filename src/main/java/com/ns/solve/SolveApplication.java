package com.ns.solve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SolveApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolveApplication.class, args);
    }

}
