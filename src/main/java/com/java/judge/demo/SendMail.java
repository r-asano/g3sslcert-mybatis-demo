package com.java.judge.demo;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.java.judge.mapper.ReadMapper;

@Component
public class SendMail {

//    →メール本文の文字コードはISO-2022-JP、添付ファイルは別の文字コードを
//    用いてもよいが、ヘッダでコードを明示する

	@Autowired
	ReadMapper readMapper;

	@Scheduled(cron = "59 17 * * * *")
	public String mailContent(int searchNumber,String dateString) {
		String content;

		content =
				"■G3サーバ証明書残留数■　通知\r\n"
				+ "=========================================================================================\r\n"
				+ "★調査日時		: " + dateString + "\r\n"
				+ "★対象範囲		: " + "有効期間開始日が 2019/08 - 2019/09 のサーバ証明書\r\n"
				+ "★対象件数		: " + searchNumber + "件\r\n"
				+ "★残留G3証明書数	: " + readMapper.countG3() + "件\r\n"
				+ "★DV/OV証明書数	: DV証明書 " + readMapper.countDV() + "件\r\n"
				+ "		  OV証明書 " + readMapper.countOV() + "件\r\n"
				+ "★添付ファイル	: sslcert-G3.log." + dateString + "\r\n"
				+ "		  error.sslcert-G3.log" + dateString + "\r\n"
				+ "\r\n"
				+ "																						以上";
//				+ "★統計情報		: " + "\r\n"
//				+ "-----------------------------------------------------------------------------------------\r\n"
//				+ "残存G3証明書数: 		" + judgeMapper.countG3() + " 件\r\n"
//				+ "\r\n"
//				+ "指定事業者ごとのG3証明書残存件数:\r\n"
//				+ "A社: "
//				+ "B社: "
//				+ "-----------------------------------------------------------------------------------------\r\n"
//				+ "\r\n"
//				+ "=========================================================================================\r\n"
//				+ "\r\n";
		return content;
	}

	@Scheduled(cron = "0 18 * * * *")
	public void sendMail(int searchNumber, String dateString, String path, String logFileName)
			throws UnsupportedEncodingException {

		Properties properties = new Properties();

		final String username = "asnao";
		final String password = "##Infnity22";
		final String to = "space.888pq@gmail.com";
		final String from = "asano@jprs.co.jp";
		final String charset = "ISO-2022-JP";
		String host = "smtp1.jprs.co.jp";
		String port = String.valueOf("25");
		String subject = " ■G3サーバ証明書残留状況調査■ (" + dateString + ") -- 淺野稜";
		String content = mailContent(searchNumber, dateString);

		properties.put("mail.smtp.connectiontimeout", "10000");
		properties.put("mail.smtp.timeout", "10000");

		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.debug", "true");

		// メールセッションを確立
		Session session = Session.getDefaultInstance(properties);

		// 送信メッセージを生成
		MimeMessage mimeMessage = new MimeMessage(session);

		try {
		// 送信先
		mimeMessage.setRecipients(Message.RecipientType.TO, to);
		mimeMessage.setRecipients(Message.RecipientType.CC, from);

		// 送信元
		InternetAddress fromHeader = new InternetAddress(from);
		mimeMessage.setFrom(fromHeader);

		// 件名
		mimeMessage.setSubject(subject, charset);

		// マルチパートオブジェクトを生成
		Multipart multipart = new MimeMultipart();

		// 本文を指定
		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setText(content, charset);
		mbp1.setHeader(from, "Ryo Asano");
		multipart.addBodyPart(mbp1);

		// ログファイルの添付
		MimeBodyPart mbp2 = new MimeBodyPart();
		FileDataSource fs1 = new FileDataSource(path + logFileName);
		mbp2.setDataHandler(new DataHandler(fs1));
		mbp2.setFileName(MimeUtility.encodeWord(fs1.getName()));
		multipart.addBodyPart(mbp2);

		// エラーログファイルの添付
		MimeBodyPart mbp3 = new MimeBodyPart();
		FileDataSource fs2 = new FileDataSource(path + "error." + logFileName);
		mbp3.setDataHandler(new DataHandler(fs2));
		mbp3.setFileName(MimeUtility.encodeWord(fs2.getName()));
		multipart.addBodyPart(mbp3);

//		List<String> attachFiles = new ArrayList<String>();
//		attachFiles.add(path + logFileName);
//		File file = new File(path + "error." + logFileName);
//		if (file.exists()) {
//			attachFiles.add(path + "error." + logFileName);
//		}
//
//		for (String attach : attachFiles) {
//			// 添付するファイルデータソースを指定
//			FileDataSource fs = new FileDataSource(attach);
//			mbp2.setDataHandler(new DataHandler(fs));
//			mbp2.setFileName(MimeUtility.encodeWord(fs.getName()));
//			multipart.addBodyPart(mbp2);
//		}


		// マルチパートオブジェクトをメッセージに設定
		mimeMessage.setContent(multipart);
		// メール送信日時
		mimeMessage.setSentDate(new Date());

		Transport transport = session.getTransport("smtp");

		//認証用ユーザ名とパスワードを設定しコネクト
		transport.connect(username, password);

		//メール送信
		transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
		transport.close();

		} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
		} catch (MessagingException e) {
		e.printStackTrace();
		}
	}

}