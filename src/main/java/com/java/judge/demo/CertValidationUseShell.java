package com.java.judge.demo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.java.judge.dto.DomainDto;
import com.java.judge.mapper.JudgeMapper;


@Component
@Transactional
public class CertValidationUseShell {

	@Autowired
	JudgeMapper judgeMapper;

	String sh = "C:/Eclips4.6/pleiades_2019/workspace/g3sslcert-mybatis-demo/src/main/resources/opensslG3.sh";

	@Scheduled(cron = "0 0 * * * *")
	public void CertG3Validation(String logFileName, String dateString, String path) throws Exception {

		try {
			// 証明書ステータスがG3,bfG2のレコードを指定
			List<DomainDto> certG3 = judgeMapper.selectCertG3();
			List<String> cnlist = new ArrayList<>();

			for (DomainDto cert : certG3) {
				// wildcard_flagの判定
				// 特定レコードのCNを抽出
				if (judgeMapper.wildcardFlag()) {
					cnlist.add(judgeMapper.Fqdn(cert));
					cnlist.add("www." + judgeMapper.Fqdn(cert));
				} else {
					cnlist.add(cert.getDnCn());
				}

				for (String cn : cnlist) {

					// CNを実行シェル（openssl）の引数として指定し、証明書の世代を抽出
					ProcessBuilder pb = new ProcessBuilder(sh, cn);
					Process p = pb.start();

					// InputStreamのスレッド開始
					// InputStreamだとバッファサイズ上限が1024 bytesのためスレッドを使用
					InputStreamThread it = new InputStreamThread(p.getInputStream());
					InputStreamThread et = new InputStreamThread(p.getErrorStream());
					it.start();
					et.start();


					//プロセスの終了待ち
					p.waitFor();

					//InputStreamのスレッド終了待ち
					it.join();
					et.join();

					// 実行結果がG4の場合テーブルを更新（証明書ステータス、更新日時の更新）、それ以外ならそのまま
					if (it.getStringList().contains("G4")) {
						Timestamp writeDate = new Timestamp(System.currentTimeMillis());
						cert.setRecUpdDate(writeDate);
						judgeMapper.updateStatusToG4(cert);
					}

					// 標準エラー出力がある場合エラーログに出力
					if (et.getStringList() != null || et.getStringList().size() != 0) {
						ErrorLog.outputErrorLog(et.getStringList(), logFileName,  dateString, path, cn);
					}

					p.destroy();  // 子プロセスを明示的に終了させ、資源を回収できるようにする

					// Delay
					Thread.sleep(1000); // 1秒(1000ミリ秒)間だけ処理を止める

					// リストの初期化
					cnlist.clear();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}


	// 全数検索時にはステータスに関係なくレコードを抜き出す
	public void CertAllValidation(String logFileName, String dateString, String path) {

		try {
			// 証明書ステータスがG3,bfG2のレコードを指定
			List<DomainDto> certAll = judgeMapper.selectAllDomain();
			List<String> cnlist = new ArrayList<>();


			for (DomainDto cert : certAll) {
				// wildcard_flagの判定
				// 特定レコードのCNを抽出
				if (judgeMapper.wildcardFlag()) {
					cnlist.add(judgeMapper.Fqdn(cert));
					cnlist.add("www." + judgeMapper.Fqdn(cert));
				} else {
					cnlist.add(cert.getDnCn());
				}

				for (String cn : cnlist) {

					// CNを実行シェル（openssl）の引数として指定し、証明書の世代を抽出
					ProcessBuilder pb = new ProcessBuilder(sh, cn);
					Process p = pb.start();

					//InputStreamのスレッド開始
					InputStreamThread it = new InputStreamThread(p.getInputStream());
					InputStreamThread et = new InputStreamThread(p.getErrorStream());
					it.start();
					et.start();

					//プロセスの終了待ち
					p.waitFor();

					//InputStreamのスレッド終了待ち
					it.join();
					et.join();

					// 実行結果がG4の場合テーブルを更新（証明書ステータス、更新日時の更新）、それ以外ならそのまま
					if (it.getStringList().contains("G4")) {
						Timestamp writeDate = new Timestamp(System.currentTimeMillis());
						cert.setRecUpdDate(writeDate);
						judgeMapper.updateStatusToG4(cert);
					}

					// 標準エラー出力がある場合エラーログに出力
					if (et.getStringList() != null || et.getStringList().size() != 0) {
						ErrorLog.outputErrorLog(et.getStringList(), logFileName,  dateString, path, cn);
					}

					System.out.println("exit: " + p.exitValue());
					p.destroy();  // 子プロセスを明示的に終了させ、資源を回収できるようにする

					// Delay
					Thread.sleep(1000); // 1秒(1000ミリ秒)間だけ処理を止める

					// リストの初期化
					cnlist.clear();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

	}

}
