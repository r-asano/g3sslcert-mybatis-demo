package com.java.judge.mail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
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
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws TemplateException
     */
    public void sendMail(int searchNumber) throws MessagingException, UnsupportedEncodingException {

        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String logFileName = prefix + dateString;
        String getCertLogFileName = "getCert." + logFileName;

        // メールに添付するファイルのオブジェクトを生成
        FileSystemResource logFileResource = new FileSystemResource(path + logFileName);
        FileSystemResource getCertLogFileResource = new FileSystemResource(path + getCertLogFileName);

        // メッセージクラス生成
        MimeMessage mimeMsg = mailSender.createMimeMessage();
//        // メッセージ情報をセットするためのヘルパークラスを生成(添付ファイル使用時の第2引数はtrue)
//        MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, true, ENCODE);

        VelocityContext context = new VelocityContext();
        context.put("dateString", dateString);
        context.put("searchNumber", searchNumber);
        context.put("countG3", readMapper.countG3());
        context.put("countDV", readMapper.countDV());
        context.put("countOV", readMapper.countOV());

        StringWriter writer = new StringWriter();
        velocityEngine.mergeTemplate("src/main/resources/templates/g3mail.vm", ENCODE, context, writer);

//        helper.setText(writer.toString());
//        helper.setFrom(FROM);
//        helper.setTo(TO);
//        helper.setCc(CC);
//        helper.setSubject("■G3サーバ証明書残留状況調査■ (" + dateString + ") -- 淺野稜");
//
//        helper.addAttachment(logFileName, logFileResource);
//        helper.addAttachment(getCertLogFileName, getCertLogFileResource);

        mimeMsg.addFrom(InternetAddress.parse(FROM));
        mimeMsg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO));
        mimeMsg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC));
        mimeMsg.setSubject("■G3サーバ証明書残留状況調査■ (" + dateString + ") -- 淺野 稜", ENCODE);
        mimeMsg.setSentDate(new Date());
        mimeMsg.setHeader("Content-Transfer-Encoding", "7bit");

        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText(writer.toString(), ENCODE, "plain");

        MimeBodyPart mbp2 = new MimeBodyPart();
        File attachmentFile1 = new File(path + logFileName);
        FileDataSource fds1 = new FileDataSource(attachmentFile1);
        mbp2.setDataHandler(new DataHandler(fds1));
        mbp2.setFileName(MimeUtility.encodeWord(fds1.getName()));

//        MimeBodyPart mbp3 = new MimeBodyPart();
//        File attachmentFile2 = new File(path + getCertLogFileName);
//        FileDataSource fds2 = new FileDataSource(attachmentFile2);
//        mbp2.setDataHandler(new DataHandler(fds2));
//        mbp2.setFileName(MimeUtility.encodeWord(fds2.getName()));

        Multipart mp = new MimeMultipart();
        mp.addBodyPart(mbp1);
        mp.addBodyPart(mbp2);
//        mp.addBodyPart(mbp3);

        mimeMsg.setContent(mp, "text/html;charset=iso-2022-jp");


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