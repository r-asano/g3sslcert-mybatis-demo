package com.java.judge.mail;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VelocityBean {

    @Bean
    public VelocityEngine velocityEngine() {
        return new VelocityEngine();
    }
}
