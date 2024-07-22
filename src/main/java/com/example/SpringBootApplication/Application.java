package com.example.SpringBootApplication;

import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements org.springframework.boot.CommandLineRunner {

	public static final Logger logger = org.slf4j.LoggerFactory.getLogger(Application.class);
	public static void main(String[] args) {
		logger.info("==================== Application started ====================");
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		logger.info("==================== Application running ====================");
		System.out.println("============================================================ Hello World! ============================================================");
	}

	public void testSonarQube() {
		String unusedVariable = "This variable is not used";
		System.out.println("This is a test for SonarQube");
	}
}
