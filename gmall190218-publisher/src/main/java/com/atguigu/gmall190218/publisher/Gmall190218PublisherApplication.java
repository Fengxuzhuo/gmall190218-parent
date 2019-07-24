package com.atguigu.gmall190218.publisher;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall190218.publisher.mapper")
public class Gmall190218PublisherApplication {

    public static void main(String[] args) {
        SpringApplication.run(Gmall190218PublisherApplication.class, args);
    }

}
