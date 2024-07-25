package com.example.SpringBootApplication;

import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

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

	public void testSonarQubeError() throws IOException {
		FileInputStream file = null;
		try {
			file = new FileInputStream("non_existent_file.txt");
		} catch (FileNotFoundException e) {
			// Do nothing
			logger.error("Error: File not found.", e);
		} finally {
			if (file != null) {
				file.close();
			}
		}
	}
}
