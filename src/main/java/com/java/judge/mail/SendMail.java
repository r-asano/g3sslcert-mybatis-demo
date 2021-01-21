package com.java.judge.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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
    private MailConfig mailSender;

    @Autowired
    private VelocityEngine velocityEngine;

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
        String getCertLogFileName = "getCert." + logFileName;

        // メールに添付するファイルのオブジェクトを生成
        FileSystemResource logFileResource = new FileSystemResource(path + logFileName);
        FileSystemResource errorLogFileResource = new FileSystemResource(path + getCertLogFileName);

        // メッセージクラス生成
        MimeMessage mimeMsg = mailSender.createMimeMessage();
        // メッセージ情報をセットするためのヘルパークラスを生成(添付ファイル使用時の第2引数はtrue)
        MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true);

        VelocityContext context = new VelocityContext();
        context.put("dateString", dateString);
        context.put("searchNumber", searchNumber);
        context.put("countG3", readMapper.countG3());
        context.put("countDV", readMapper.countDV());
        context.put("countOV", readMapper.countOV());

        StringWriter writer = new StringWriter();
        velocityEngine.mergeTemplate("src/main/resources/templates/g3mail.vm", ENCODE, context, writer);
        helper.setText(writer.toString());

        helper.setFrom(FROM);
        helper.setTo(TO);
        helper.setCc(CC);
        helper.setSubject("■G3サーバ証明書残留状況調査■ (" + dateString + ") -- 淺野稜");

        helper.addAttachment(logFileName, logFileResource);
        helper.addAttachment(getCertLogFileName, errorLogFileResource);

        // メール送信
        try {
            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withRegion(Regions.AP_NORTHEAST_1).build();

            // Send the email.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mimeMsg.writeTo(outputStream);
            RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

            SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);

            client.sendRawEmail(rawEmailRequest);
        } catch (Exception ex) {
            System.out.println("Email Failed");
            System.err.println("Error message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}