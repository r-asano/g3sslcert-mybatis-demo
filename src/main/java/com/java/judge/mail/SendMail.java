package com.java.judge.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws TemplateException
     */
    public void sendMail(int searchNumber) throws MessagingException, UnsupportedEncodingException {

        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String logFileName = prefix + dateString;
        //        String getCertLogFileName = "getCert." + logFileName;
        //
        //        // メールに添付するファイルのオブジェクトを生成
        //        FileSystemResource logFileResource = new FileSystemResource(path + logFileName);
        //        FileSystemResource getCertLogFileResource = new FileSystemResource(path + getCertLogFileName);

        // メッセージクラス生成
        MimeMessage message = mailSender.createMimeMessage();

        // メッセージ情報をセットするためのヘルパークラスを生成(添付ファイル使用時の第2引数はtrue)
        //                MimeMessageHelper helper = new MimeMessageHelper(message, true, ENCODE);

        Velocity.setProperty("file.resource.loader.path", "src/main/resources/templates/");
        //Velocityの初期化
        Velocity.init();

        VelocityContext context = new VelocityContext();
        context.put("dateString", dateString);
        context.put("searchNumber", searchNumber);
        context.put("countG3", readMapper.countG3());
        context.put("countDV", readMapper.countDV());
        context.put("countOV", readMapper.countOV());

        StringWriter writer = new StringWriter();
        Template template = Velocity.getTemplate("g3mail.vm", ENCODE);
        template.merge(context, writer);

        //
        //        helper.setText(writer.toString());
        //        helper.setFrom(FROM);
        //        helper.setTo(TO);
        //        helper.setSubject("■G3サーバ証明書残留状況調査■ (" + dateString + ") -- 淺野稜");
        //
        //        helper.addAttachment(logFileName, logFileResource);
        ////        helper.addAttachment(getCertLogFileName, getCertLogFileResource);

        String SUBJECT = "■G3サーバ証明書残留状況調査■ (" + dateString + ") -- 淺野 稜";

        // The email body for recipients with non-HTML email clients.
        String BODY_TEXT = writer.toString();

        String ATTACHMENT1 = path + logFileName;
        String ATTACHMENT2 = path + "getCert." + logFileName;

        // Add subject, from and to lines.
        message.setSubject(SUBJECT, ENCODE);
        message.setFrom(new InternetAddress(FROM));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO));

        // Create a multipart/alternative child container.
        MimeMultipart msg_body = new MimeMultipart("alternative");

        // Create a wrapper for the HTML and text parts.
        MimeBodyPart wrap = new MimeBodyPart();

        // Define the text part.
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(BODY_TEXT, "text/plain; charset=" + ENCODE);

        // Add the text and HTML parts to the child container.
        msg_body.addBodyPart(textPart);

        // Add the child container to the wrapper object.
        wrap.setContent(msg_body);

        // Create a multipart/mixed parent container.
        MimeMultipart msg = new MimeMultipart("mixed");

        // Add the parent container to the message.
        message.setContent(msg);

        // Add the multipart/alternative part to the message.
        msg.addBodyPart(wrap);

        // Define the attachment
        MimeBodyPart att1 = new MimeBodyPart();
        DataSource fds1 = new FileDataSource(ATTACHMENT1);
        att1.setDataHandler(new DataHandler(fds1));
        att1.setFileName(fds1.getName());

        // Add the attachment to the message.
        msg.addBodyPart(att1);

        // Define the attachment
        MimeBodyPart att2 = new MimeBodyPart();
        DataSource fds2 = new FileDataSource(ATTACHMENT2);
        att1.setDataHandler(new DataHandler(fds2));
        att1.setFileName(fds2.getName());

        // Add the attachment to the message.
        msg.addBodyPart(att2);

        // メール送信
        try {
            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withRegion(Regions.AP_NORTHEAST_1).build();

            // Send the email.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);
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