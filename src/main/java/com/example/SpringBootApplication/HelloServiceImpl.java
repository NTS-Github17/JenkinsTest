package com.example.SpringBootApplication;

import org.springframework.stereotype.Service;

@Service
public class HelloServiceImpl implements HelloService{

        @Override
        public String getHelloGreetings(String name) {
            return String.format("Welcome to Resdii, %s!", name);
        }
}
