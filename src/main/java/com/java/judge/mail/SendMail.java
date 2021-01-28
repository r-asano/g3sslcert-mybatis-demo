package com.java.judge.mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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
    private ReadMapper readMapper;

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Value("${app.path}")
    private String path;

    @Value("${app.getCertPrefix}")
    private String getCertPrefix;

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

    /**
     * メール送信
     *
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws TemplateException
     */
    public void sendMail(int searchNumber, String prefixAll, String dateString)
            throws MessagingException, UnsupportedEncodingException {

        String remainG3LogFile = prefixAll + remainG3Prefix + dateString;
        String getCertLogFile = prefixAll + getCertPrefix + dateString;

        // メールに添付するファイルのオブジェクトを生成
        FileSystemResource G3logFileResource = new FileSystemResource(path + remainG3LogFile);
        FileSystemResource getCertLogFileResource = new FileSystemResource(path + getCertLogFile);

        // メッセージクラス生成
        MimeMessage message = mailSender.createMimeMessage();

        // メッセージ情報をセットするためのヘルパークラスを生成(添付ファイル使用時の第2引数はtrue)
        // ENCODEの設定はspring.mail.default-encoding設定が引き継がれないので、明示的に設定
        MimeMessageHelper helper = new MimeMessageHelper(message, true, ENCODE);

        String SUBJECT = "■G3サーバ証明書残留状況調査■ (" + dateString + ") -- 淺野 稜";

        String BODY_TEXT = mailContext(dateString, remainG3LogFile, getCertLogFile, searchNumber);

        // mimemessageの設定
        helper.setText(BODY_TEXT);
        helper.setFrom(FROM);
        helper.setTo(TO);
        helper.setSubject(SUBJECT);
        helper.addAttachment(remainG3LogFile, G3logFileResource);
        helper.addAttachment(getCertLogFile, getCertLogFileResource);

        message.addHeader("Attachment-File-Charset", "UTF-8");

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

    /*
     * メール本文
     */
    public String mailContext(String dateString, String remainG3LogFile, String getCertLogFile, int searchNumber) {

        StringBuilder sb = new StringBuilder();

        sb.append(
                "■G3サーバ証明書残留数■  通知\r\n"
                        + "=========================================================================================\r\n"
                        + "★調査日時        : " + dateString + "\r\n"
                        + "★対象範囲        : 有効期間開始日が 2019/08 - 2019/09 のサーバ証明書\r\n"
                        + "★対象件数        : " + searchNumber + "件\r\n"
                        + "★残留G3証明書数  : " + readMapper.countG3() + " 件\r\n"
                        + "★DV/OV証明書数   : DV証明書" + readMapper.countDV() + " 件\r\n"
                        + "                    OV証明書" + readMapper.countOV() + " 件\r\n"
                        + "★添付ファイル    : " + remainG3LogFile + "\r\n"
                        + "                    " + getCertLogFile + "\r\n"
                        + "\r\n"
                        + "★指定事業者ごとの残留G3証明書数\r\n"
                        + "joint_agent_id, agent_name, amount_of_G3\r\n"
                        + "------------------------------------------------------------\r\n");

        // mapでagent_id,count(*)を取得
        List<Map<String, Object>> countMap = readMapper.countG3GroupByAgent();

        // for文でagent_id, agent_name, count(*)をappend
        for (Map<String, Object> entry  : countMap) {
            sb.append(
                    entry.get("id") + ", "
                    + readMapper.selectAgentName((String) entry.get("id")) + ", "
                    + entry.get("count") + "\r\n"
                    );
        }


        sb.append(
                "\r\n"
                        + "以上\r\n"
                        + "-------------------\r\n"
                        + "asano@jprs.co.jp\r\n"
                        + "開発部  淺野 稜\r\n"
                        + "-------------------");

        String BODY_TEXT = sb.toString();

        return BODY_TEXT;
    }
}