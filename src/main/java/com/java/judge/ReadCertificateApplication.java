//package com.java.judge;
//
//import java.io.IOException;
//
//import org.mybatis.spring.annotation.MapperScan;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//
//import com.java.judge.read.InitInsertRecord;
//
//
//@SpringBootApplication
//@MapperScan(basePackages = "com.example.mapper")
//public class ReadCertificateApplication {
//
//	public static void main(String[] args) throws IOException, InterruptedException {
//
//		ClassPathXmlApplicationContext context =
//				new ClassPathXmlApplicationContext("applicationContext.xml");
//
//		// TSVから読みだしてAgentテーブルにInsert
//		InitInsertRecord insert = context.getBean(InitInsertRecord.class);
//
//		insert.InsertAgent();
//		insert.InsertCertificate();
//		insert.InsertDomain();
//
//		context.close();
//	}
//}