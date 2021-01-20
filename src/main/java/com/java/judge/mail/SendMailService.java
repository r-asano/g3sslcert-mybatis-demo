package com.java.judge.mail;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Service
public class SendMailService {

    @Autowired
    MailConfig mailSender;

    @Value("${app.path}")
    private String path;

    @Value("${app.logFilePrefix}")
    private String prefix;

    @Value("${mail.to}")
    private String TO;

    @Value("${mail.cc}")
    private String CC;

    @Value("${mail.from}")
    private String FROM;

    @Value("${mail.encoding}")
    private String ENCODE;

    public MimeMessage getMimeMsg(Context context) throws MessagingException {

        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String logFileName = prefix + dateString;
        String errorLogFileName = "error." + logFileName;

        // メールに添付するファイルのオブジェクトを生成
        FileSystemResource logFileResource = new FileSystemResource(path + logFileName);
        FileSystemResource errorLogFileResource = new FileSystemResource(path + errorLogFileName);

        // メッセージクラス生成
        MimeMessage mimeMsg = mailSender.createMimeMessage();
        // メッセージ情報をセットするためのヘルパークラスを生成(添付ファイル使用時の第2引数はtrue)
        MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true);

        helper.setFrom(FROM);
        helper.setTo(TO);
        helper.setCc(CC);
        helper.setSubject("■G3サーバ証明書残留状況調査■ (" + dateString + ") -- 淺野稜");
        helper.setSentDate(new Date());

        helper.setText(getMailBody("g3mail", context), true);
        helper.addAttachment(logFileName, logFileResource);
        helper.addAttachment(errorLogFileName, errorLogFileResource);

        return mimeMsg;
    }

    private String getMailBody(String templateName, Context context) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(mailTemplateResolver());
        return templateEngine.process(templateName, context);

    }

    private ClassLoaderTemplateResolver mailTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding(ENCODE);
        templateResolver.setCacheable(true);
        return templateResolver;
    }

}