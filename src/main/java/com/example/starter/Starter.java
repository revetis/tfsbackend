package com.example.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(value = "com.example")
@EnableCaching
@EntityScan(value = "com.example")
@EnableJpaRepositories(value = "com.example")
public class Starter {

	public static void main(String[] args) {
		SpringApplication.run(Starter.class, args);
	}

}
