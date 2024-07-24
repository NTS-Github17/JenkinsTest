package com.example.SpringBootApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private HelloService helloService;

    @GetMapping("/hello")
    public String hello(String name){
        return helloService.getHelloGreetings(name);
    }

    @Autowired
    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }
}
