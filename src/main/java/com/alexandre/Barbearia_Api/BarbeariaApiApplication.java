package com.alexandre.Barbearia_Api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;
import java.time.ZoneId;


@SpringBootApplication
@EnableScheduling
public class BarbeariaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarbeariaApiApplication.class, args);
        System.out.println("Timezone JVM: " + ZoneId.systemDefault());
        System.out.println("Agora: " + LocalDateTime.now());

    }

}
