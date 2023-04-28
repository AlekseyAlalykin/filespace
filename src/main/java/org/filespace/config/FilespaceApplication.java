package org.filespace.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@PropertySource("classpath:application.properties")
@EntityScan("org.filespace.model")
@EnableJpaRepositories("org.filespace.repositories")
@ComponentScan("org.filespace")
public class FilespaceApplication{

	public static void main(String[] args) {
		SpringApplication.run(FilespaceApplication.class, args);
	}

}
