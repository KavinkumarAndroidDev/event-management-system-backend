package com.project.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventManagementsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventManagementsystemApplication.class, args);
	}

}
