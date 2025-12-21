package com.example.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import com.example.apps.notifications.utils.N8NProperties;
import com.example.tfs.ApplicationProperties;
import com.vaadin.flow.spring.annotation.EnableVaadin;

@SpringBootApplication
@ComponentScan(value = "com.example")
@EnableCaching
@EntityScan(value = "com.example")
@EnableAsync
@EnableJpaRepositories(value = "com.example")
@EnableConfigurationProperties({ ApplicationProperties.class, N8NProperties.class })
@EnableVaadin(value = "com.example")
@EnableElasticsearchRepositories(basePackages = {
		"com.example.apps.products.repositories.search",
		"com.example.apps.orders.repositories.search"
})
public class Starter {

	public static void main(String[] args) {
		SpringApplication.run(Starter.class, args);
		System.out.println("___________________________________");
		System.out.println("THEFIRSTSTEP SUNUCUSU BAŞLATILDI");
		System.out.println("___________________________________");
	}

}
