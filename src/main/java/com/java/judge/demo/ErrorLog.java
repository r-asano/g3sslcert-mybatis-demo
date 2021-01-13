package com.java.judge.demo;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ErrorLog {

	// エラー発生時に実行結果の全文をエラーログファイルに出力する
	public static void outputErrorLog(List<String> errorLogList, String logFileName, String dateString, String path, String cn) {
		String errorFileName = "error." + logFileName + dateString;

			try (FileWriter errorFile = new FileWriter(path + errorFileName, true)) {
				errorFile.write("エラー発生CN: " + cn);
				errorFile.write("エラー発生日時: " + new Timestamp(System.currentTimeMillis()));
				errorFile.write("\nエラー内容:");

				for (String s : errorLogList) {
					errorFile.write("\n" + s);
				}

				errorFile.write("\n");

				errorFile.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	// 全数検査の場合
	public static void outputErrorLogAll(List<String> errorLogList, String logFileName, String dateString, String path, String cn) {
		String errorFileName = "all.error." + logFileName + dateString;

			try (FileWriter errorFile = new FileWriter(path + errorFileName, true)) {
				errorFile.write("エラー発生CN: " + cn);
				errorFile.write("\nエラー内容:");

				for (String s : errorLogList) {
					errorFile.write("\n" + s);
				}

				errorFile.write("\n");
				errorFile.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}


}
