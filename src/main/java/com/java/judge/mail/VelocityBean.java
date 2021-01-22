package com.java.judge.mail;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VelocityBean {

    @Value("${mail.encoding}")
    private String ENCODE;

    @Bean
    public VelocityEngine velocityEngine() {

        VelocityEngine engine = new VelocityEngine();

//        Properties p = new Properties();
//        p.setProperty("input.encoding", ENCODE);
//        p.setProperty("output.encoding", ENCODE);
//        p.setProperty("resource.loader", "file");
////        p.setProperty("file.resource.loader.path", ""); // ※このプロパティの値を空にしたいが、設定ファイルだと上手く空にならない。。。このためPropertiesで設定する
////        p.setProperty("file.resource.loader.class", FileResourceLoader.class.getTypeName());
////        p.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
////        p.setProperty("runtime.log.logsystem.log4j.category", "velocity");
////        p.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
//        engine.init(p);

        return engine;
    }
}
