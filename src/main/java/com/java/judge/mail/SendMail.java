package com.java.judge.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
        String getCertLogFileName = "getCert." + logFileName;

        // メールに添付するファイルのオブジェクトを生成
        FileSystemResource logFileResource = new FileSystemResource(path + logFileName);
        FileSystemResource getCertLogFileResource = new FileSystemResource(path + getCertLogFileName);

        // メッセージクラス生成
        MimeMessage message = mailSender.createMimeMessage();

        // メッセージ情報をセットするためのヘルパークラスを生成(添付ファイル使用時の第2引数はtrue)
        MimeMessageHelper helper = new MimeMessageHelper(message, true, ENCODE);

        String SUBJECT = "■G3サーバ証明書残留状況調査■ (" + dateString + ") -- 淺野 稜";

        String BODY_TEXT = "■G3サーバ証明書残留数■  通知\r\n"
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

        helper.setText(BODY_TEXT);
        helper.setFrom(FROM);
        helper.setTo(TO);
        helper.setSubject(SUBJECT);
        helper.addAttachment(logFileName, logFileResource);
        helper.addAttachment(getCertLogFileName, getCertLogFileResource);

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