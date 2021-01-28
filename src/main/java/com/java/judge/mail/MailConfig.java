package com.java.judge.mail;

import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig extends JavaMailSenderImpl {

//    @Value("${spring.mail.host}")
//    private String HOST;
//
//    @Value("${spring.mail.port}")
//    private Integer PORT;
//
//    @Value("${spring.mail.protocol}")
//    private String PROTOCOL;
//
//    @Value("${spring.mail.username}")
//    private String AWS_ID;
//
//    @Value("${spring.mail.password}")
//    private String AWS_SECRET;
//
//    @Value("${mail.encoding}")
//    private String ENCODE;
//
//    @Bean()
//    public JavaMailSender javaMailService() {
//        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
//
//        javaMailSender.setHost(HOST);
//        javaMailSender.setPort(PORT);
//        javaMailSender.setProtocol(PROTOCOL);
//        javaMailSender.setUsername(AWS_ID);
//        javaMailSender.setPassword(AWS_SECRET);
//        javaMailSender.setDefaultEncoding(ENCODE);
//
//        return javaMailSender;
//    }
}
