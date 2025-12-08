package com.example.starter;

import com.example.configuration.GlobalConfigurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableConfigurationProperties(GlobalConfigurations.class)
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
