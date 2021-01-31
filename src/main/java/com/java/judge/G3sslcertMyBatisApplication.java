package com.java.judge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@MapperScan(basePackages = "com.java.judge.mapper")
public class G3sslcertMyBatisApplication {

    public static void main(String[] args) throws Exception {
        new SpringApplicationBuilder(G3sslcertMyBatisApplication.class)
        .web(WebApplicationType.NONE).run(args);
    }
}
