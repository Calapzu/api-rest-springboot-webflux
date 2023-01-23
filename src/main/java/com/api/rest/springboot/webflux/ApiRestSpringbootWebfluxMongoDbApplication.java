package com.api.rest.springboot.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
public class ApiRestSpringbootWebfluxMongoDbApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiRestSpringbootWebfluxMongoDbApplication.class, args);
    }

}
