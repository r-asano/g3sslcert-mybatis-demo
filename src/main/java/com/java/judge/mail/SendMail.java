package com.java.judge.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

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
//                        MimeMessageHelper helper = new MimeMessageHelper(message, true, ENCODE);


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
        String BODY_TEXT =
                "■G3サーバ証明書残留数■  通知\r\n"
                + "=========================================================================================\r\n"
                + "★調査日時        : " + dateString + "\r\n"
                + "★対象範囲        : 有効期間開始日が 2019/08 - 2019/09 のサーバ証明書\r\n"
                + "★対象件数        : " + searchNumber + "件\r\n"
                + "★残留G3証明書数  : " + readMapper.countG3() + "件\r\n"
                + "★DV/OV証明書数   : DV証明書" + readMapper.countDV() + "件\r\n"
                + "                    OV証明書" + readMapper.countOV() + "件\r\n"
                + "★添付ファイル    : sslcert-G3.log." + dateString + "\r\n"
                + "                    getCert.sslcert-G3.log." + dateString + "\r\n"
                + "\r\n"
                + "以上\r\n"
                + "-------------------\r\n"
                + "From : asano@jprs.co.jp\r\n"
                + "開発部  淺野 稜\r\n"
                + "-------------------";

        List<String> ATTACHMENTS = new ArrayList<String>();
        ATTACHMENTS.add(path + logFileName);
        ATTACHMENTS.add(path + "getCert." + logFileName);

        // Add subject, from and to lines.
        message.setSubject(SUBJECT, ENCODE);
        message.setFrom(new InternetAddress(FROM));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO));

        // Create a multipart/alternative child container.
        MimeMultipart msg_body = new MimeMultipart();

        // Create a wrapper for the HTML and text parts.
        MimeBodyPart wrap = new MimeBodyPart();

        // Define the text part.
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(BODY_TEXT, "text/plain; charset=" + ENCODE);

        // Add the text and HTML parts to the child container.
        msg_body.addBodyPart(textPart);

        // Add the child container to the wrapper object.
        wrap.setContent(msg_body);

        // Create a multipart/mixed parent container.(mixed:添付ファイルあり)
        MimeMultipart msg = new MimeMultipart("mixed");

        message.setContent(msg);

        msg.addBodyPart(wrap);

        for (String file : ATTACHMENTS) {
            MimeBodyPart bp = new MimeBodyPart();
            DataSource fds = new FileDataSource(file);
            bp.setDataHandler(new DataHandler(fds));
            bp.setFileName(fds.getName());
            msg.addBodyPart(bp);
        }

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