package com.java.judge.demo;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.java.judge.mapper.ReadMapper;

@Component
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
	 */
	public void sendMail(int searchNumber)
			throws MessagingException {

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
		helper.setFrom("asano@jprs.co.jp");
		// 送信先アドレスをセット
		helper.setTo("space.888pq@gmail.com");
		helper.setCc("asano@jprs.co.jp");
		// 表題をセット
		helper.setSubject("■G3サーバ証明書残留状況調査■ (" + dateString + ") -- 淺野稜");
		// 本文をセット
		helper.setText(mailContent(searchNumber, dateString));
		// 添付ファイルをセット
		helper.addAttachment(logFileName, logFileResource);
		helper.addAttachment(errorLogFileName, errorLogFileResource);

		// メール送信
		mailSender.send(mimeMsg);
	}
}