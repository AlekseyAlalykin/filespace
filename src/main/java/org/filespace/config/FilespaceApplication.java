package org.filespace.config;


import org.filespace.model.User;
import org.filespace.services.FileService;
import org.filespace.services.IntegratedService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication//(exclude = {SecurityAutoConfiguration.class})
@PropertySource("classpath:application.properties")
@EntityScan("org.filespace.model")
@EnableJpaRepositories("org.filespace.repositories")
public class FilespaceApplication {

	public static void main(String[] args) {

		/*
		String message = "user_";
		Pattern pattern = Pattern.compile(
				"^[A-Za-z0-9]+(?:[ _-][A-Za-z0-9]+)*$",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(message);
		System.out.println(matcher.matches());

		 */


		//AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(FilespaceContextConfig.class);

		//FileService fileService = context.getBean(FileService.class);

		//System.out.println(fileService.getRootDirectory());
		/*
		User user1 = new User("name1","password1","salt1","email1", LocalDate.now());
		File file1 = new File(user1,"filename1",1024L,LocalDate.now(), LocalTime.now(),
				0,"comment1","somehashvalue1");
		*/


		//SessionFactory sessionFactory = HibernateUtil.getSessionFactory();



		SpringApplication.run(FilespaceApplication.class, args);
	}


}
