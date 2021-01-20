package com.java.judge.demo;

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
    ReadMapper readMapper;

    // メール送信クラス
    @Autowired
    JavaMailSenderImpl mailSender;

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
                "■G3サーバ証明書残留数■　通知\r\n"
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
    public void sendMail(int searchNumber)
            throws MessagingException, IOException {

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

        // 送信元アドレスをセット
        helper.setFrom(FROM);
        // 送信先アドレスをセット
        helper.setTo(TO);
        helper.setCc(CC);
        // 表題をセット
        helper.setSubject("■G3サーバ証明書残留状況調査■ (" + dateString + ") -- 淺野稜");
//        // 本文セット
//        freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates/");
//        Template template = freemarkerConfig.getTemplate("mailTemplate.ftl");
//        Map<String, Object> mailProperty = new HashMap<String, Object>();
//        mailProperty.put("dateString", dateString);
//        mailProperty.put("searchNumber", searchNumber);
//        mailProperty.put("countG3", readMapper.countG3());
//        mailProperty.put("countDV", readMapper.countDV());
//        mailProperty.put("countOV", readMapper.countOV());
//        String text = FreeMarkerTemplateUtils.processTemplateIntoString(template, mailProperty);
// 	      helper.setText(text, true);

        // 本文をセット
        helper.setText(mailContent(searchNumber, dateString));
        // 添付ファイルをセット
        helper.addAttachment(logFileName, logFileResource);
        helper.addAttachment(errorLogFileName, errorLogFileResource);

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