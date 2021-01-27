package com.java.judge.mail;

import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VelocityBean {

    @Bean
    public VelocityEngine velocityEngine() {
        VelocityEngine Velocity = new VelocityEngine();

        Properties p = new Properties();

        p.setProperty("input.encoding", "ISO-2022-JP");
        p.setProperty("output.encoding", "ISO-2022-JP");


        Velocity.init(p);

        return Velocity;
    }
}