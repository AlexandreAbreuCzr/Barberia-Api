package com.alexandre.Barbearia_Api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.time.ZoneId;

@SpringBootApplication
public class BarbeariaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarbeariaApiApplication.class, args);
        System.out.println("Timezone JVM: " + ZoneId.systemDefault());
        System.out.println("Agora: " + LocalDateTime.now());

    }

}
