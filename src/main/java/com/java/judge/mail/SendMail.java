package com.java.judge.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.java.judge.mapper.ReadMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Service
@Log4j2
public class SendMail {

    private final ReadMapper readMapper;

    private final JavaMailSenderImpl mailSender;

    @Value("${app.path}")
    private String path;

    @Value("${app.remainG3Prefix}")
    private String remainG3Prefix;

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

    @Value("${enviroment.profile}")
    private String PROFILE;

    /**
     * メール送信
     *
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public void sendMail(int searchNumber, String prefixAll, String dateString)
            throws MessagingException, UnsupportedEncodingException {
        log.info("sendMail 開始");

        String remainG3LogFile = prefixAll + remainG3Prefix + dateString;

        // メッセージクラス生成
        MimeMessage message = mailSender.createMimeMessage();

        MimeMultipart multipart=new MimeMultipart();

        String SUBJECT = "■G3サーバ証明書残留状況調査■ (" + dateString + ") -- 淺野 稜";
        String BODY_TEXT = mailContext(dateString, remainG3LogFile, searchNumber);

        // 基本情報
        // Personalをapplication.prooerties から引くと失敗するのでハードコーディング
        message.setFrom(new InternetAddress(FROM, "残留G3サーバー証明書通知システム", ENCODE));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO));
        message.setSubject(SUBJECT, ENCODE);
        message.setSentDate(new Date());

        // パート1に本文を設定
        MimeBodyPart bodyPart1=new MimeBodyPart();
        bodyPart1.setText(BODY_TEXT, ENCODE);
        multipart.addBodyPart(bodyPart1);

        // パート2にファイルを設定
        MimeBodyPart bodyPart2=new MimeBodyPart();
        DataSource dataSource=new FileDataSource(path + remainG3LogFile);
        DataHandler dataHandler=new DataHandler(dataSource);
        bodyPart2.setDataHandler(dataHandler);
        bodyPart2.setFileName(MimeUtility.encodeWord(remainG3LogFile));
        multipart.addBodyPart(bodyPart2);

        // メールにマルチパートを設定
        message.setContent(multipart);

        // メール送信
        if (PROFILE.equals("development")) {
            try {
                mailSender.send(message);
                log.info("メール送信に成功");
                log.info("FROM: " + FROM);
                log.info("TO: " + TO);
                log.info("BODY_TEXT" + BODY_TEXT);
                log.info("sendMail 正常終了");
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        } else {
            try {
                AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
                        .withRegion(Regions.AP_NORTHEAST_1).build();

                // Send the email.
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                message.writeTo(outputStream);
                RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

                SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);

                client.sendRawEmail(rawEmailRequest);
                log.info("メール送信に成功");
                log.info("FROM: " + FROM);
                log.info("TO: " + TO);
                log.info("BODY_TEXT" + BODY_TEXT);
                log.info("sendMail 正常終了");
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }

    /*
     * メール本文
     */
    public String mailContext(String dateString, String remainG3LogFile, int searchNumber) {
        log.info("mailContext 開始");

        StringBuilder sb = new StringBuilder();

        sb.append(
                        "■G3サーバ証明書残留数■  通知\r\n"
                        + "=========================================================================================\r\n"
                        + "★調査日時        : " + dateString + "\r\n"
                        + "★対象範囲        : 有効期間開始日が 2019/08 - 2019/09 のサーバ証明書\r\n"
                        + "★対象件数        : " + searchNumber + "件\r\n"
                        + "★残留G3証明書数  : " + readMapper.countG3() + "件\r\n"
                        + "★DV/OV証明書数   : DV証明書" + readMapper.countDV() + "件\r\n"
                        + "                    OV証明書" + readMapper.countOV() + "件\r\n"
                        + "★添付ファイル    : " + remainG3LogFile + "\r\n"
                        + "\r\n"
                        + "★指定事業者ごとの残留G3証明書数\r\n"
                        + "joint_agent_id,agent_name,countG3\r\n"
                        + "------------------------------------------------------------\r\n"
                );

        // mapでagent_id,count(*)を取得
        List<Map<String, Object>> countMap = readMapper.countG3GroupByAgent();

        // for文でagent_id, agent_name, count(*)をappend
        for (Map<String, Object> entry  : countMap) {
            sb.append(
                    entry.get("id") + ","
                    + readMapper.selectAgentName((String) entry.get("id")) + ","
                    + entry.get("count") + "\r\n"
                    );
        }


        sb.append(
                        "\r\n"
                        + "以上\r\n"
                        + "-------------------\r\n"
                        + "asano@jprs.co.jp\r\n"
                        + "開発部  淺野 稜\r\n"
                        + "-------------------"
                );

        String BODY_TEXT = sb.toString();

        log.info("mailContext 正常終了");
        return BODY_TEXT;
    }
}