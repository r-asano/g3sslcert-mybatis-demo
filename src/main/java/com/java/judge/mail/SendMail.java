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
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

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

    // メール送信クラス
    @Autowired
    SendMailService sendMailService;

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

    @Value("${spring.mail.username}")
    private String AWS_ID;

    @Value("${spring.mail.password}")
    private String AWS_SECRET;


    /*
     * メール本文の設定
     */
    public String mailContent(int searchNumber,String dateString) {
        String content;

        content =
                "■G3サーバ証明書残留数■  通知\r\n"
                + "=========================================================================================\r\n"
                + "★調査日時		: " + dateString + "\r\n"
                + "★対象範囲		: " + "有効期間開始日が 2019/08 - 2019/09 のサーバ証明書\r\n"
                + "★対象件数		: " + searchNumber + "件\r\n"
                + "★残留G3証明書数	: " + readMapper.countG3() + "件\r\n"
                + "★DV/OV証明書数		: DV証明書 " + readMapper.countDV() + "件\r\n"
                + "		 	  OV証明書 " + readMapper.countOV() + "件\r\n"
                + "★添付ファイル		: sslcert-G3.log." + dateString + "\r\n"
                + "		 	  error.sslcert-G3.log" + dateString + "\r\n"
                + "\r\n"
                + "以上";
        return content;
    }


    /**
     * メール送信
     *
     * @throws MessagingException
     * @throws IOException
     * @throws TemplateException
     */
    public void sendMail(int searchNumber) throws MessagingException {

        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Context context = new Context();
        context.setVariable("dateString", dateString);
        context.setVariable("seachNumber", searchNumber);
        context.setVariable("countG3", readMapper.countG3());
        context.setVariable("countDV", readMapper.countDV());
        context.setVariable("countOV", readMapper.countOV());

        MimeMessage mimeMsg = sendMailService.getMimeMsg(context);

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