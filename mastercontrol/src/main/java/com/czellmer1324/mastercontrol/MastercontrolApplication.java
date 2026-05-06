package com.czellmer1324.mastercontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MastercontrolApplication {

    public static void main(String[] args) {
        SpringApplication.run(MastercontrolApplication.class, args);
    }

}
