package org.example.comm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.comm.mapper") // 加上这一行，指向你的mapper包路径
public class CommApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommApplication.class, args);
    }

}