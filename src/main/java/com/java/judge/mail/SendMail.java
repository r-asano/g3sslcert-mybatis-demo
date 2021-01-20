package com.java.judge.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.java.judge.mapper.ReadMapper;

@Service
public class SendMail {

    @Autowired
    ReadMapper readMapper;

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

    @Value("${spring.mail.username}")
    private String AWS_ID;

    @Value("${spring.mail.password}")
    private String AWS_SECRET;

    /**
     * メール送信
     *
     * @throws MessagingException
     * @throws IOException
     * @throws TemplateException
     */
    public void sendMail(int searchNumber) throws MessagingException {

        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String logFileName = prefix + dateString;
        String errorLogFileName = "error." + logFileName;

        // メールに添付するファイルのオブジェクトを生成
        FileSystemResource logFileResource = new FileSystemResource(path + logFileName);
        FileSystemResource errorLogFileResource = new FileSystemResource(path + errorLogFileName);

        // メッセージクラス生成
        MimeMessage mimeMsg = mailSender.createMimeMessage();
        // メッセージ情報をセットするためのヘルパークラスを生成(添付ファイル使用時の第2引数はtrue)
        MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true, ENCODE);

        helper.setFrom(FROM);
        helper.setTo(TO);
        helper.setCc(CC);
        helper.setSubject("■G3サーバ証明書残留状況調査■ (" + dateString + ") -- 淺野稜");
        helper.setSentDate(new Date());

        helper.addAttachment(logFileName, logFileResource);
        helper.addAttachment(errorLogFileName, errorLogFileResource);

        // テンプレートエンジンを使用するための設定インスタンスを生成
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

        // テンプレートエンジンの種類を指定
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding(ENCODE);

        // テンプレートエンジンを使用するためのインスタンスを生成
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver);


        Context context = new Context();
        context.setVariable("dateString", dateString);
        context.setVariable("searchNumber", searchNumber);
        context.setVariable("countG3", readMapper.countG3());
        context.setVariable("countDV", readMapper.countDV());
        context.setVariable("countOV", readMapper.countOV());

        // 使用するテンプレートのファイル名とパラメータ情報を設定します。
        String text = engine.process("/templates/g3mail.txt", context);
        helper.setText(text);

        // メール送信
        try {
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                    .withRegion(Regions.AP_NORTHEAST_1).build();

            // Send the email.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mimeMsg.writeTo(outputStream);
            RawMessage rawMessage =
                    new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

            SendRawEmailRequest rawEmailRequest =
                    new SendRawEmailRequest(rawMessage);

            client.sendRawEmail(rawEmailRequest);
        } catch (Exception ex) {
        System.out.println("Email Failed");
            System.err.println("Error message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}