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

	public void helloWorld() {
		Integer a = null;
		System.out.println(a.toString());
	}

//	 public void testSonarQube() {
//	 	String unusedVariable = "This variable is not used";
//	 	System.out.println("This is a test for SonarQube");
//	 }

//	public void testSonarQubeError() throws IOException {
//		FileInputStream file = null;
//		try {
//			file = new FileInputStream("non_existent_file.txt");
//		} catch (FileNotFoundException e) {
//			// Do nothing
//			logger.error("Error: File not found.", e);
//		} finally {
//			if (file != null) {
//				file.close();
//			}
//		}
//	}

//	public void testSonarQubeWarning() {
//		int a = 0;
//		int b = 0;
//		int c = a + b;
//		System.out.println("The sum of a and b is: " + c);
//	}

	//	public void divisionByZero() {
//		try {
//			int number = 10;
//			int result = number / 0;
//			System.out.println("Result: " + result);
//		} catch (ArithmeticException e) {
//			// Do nothing
//			System.out.println("Error: Division by zero is not allowed.");
//		}
//	}

//	public void stringToInteger() {
//		try {
//			String str = "abc";
//			int number = Integer.parseInt(str);
//			System.out.println("Converted number: " + number);
//		} catch (NumberFormatException e) {
//			System.out.println("Error: Cannot convert string to integer.");
//		}
//	}
//
//	public void nullPointerDereference() {
//		String str = null;
//		try {
//			System.out.println("Length of the string is: " + str.length());
//		} catch (NullPointerException e) {
//			// Do nothing
//			// System.out.println("Error: Attempted to dereference a null pointer.");
//		}
//	}
}
