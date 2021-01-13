package com.java.judge.demo;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.java.judge.dto.DomainDto;
import com.java.judge.mapper.ReadMapper;

@Component
public class OutputLog {

	@Autowired
	ReadMapper readMapper;

	// G3証明書ログの出力
	@Transactional
	@Scheduled(cron = "0 8 * * * *")
	public void outputLog(String logFileName, String path) {

			try (FileWriter logFile = new FileWriter(path + logFileName, true)) {

				logFile.write("更新日" + "\t\t\t" + "CN" + "\t\t\t\t" + "Status" + "\t\t\t\t\t\t" + "Agent" + "\r\n");

				// statusがG3のレコードを抽出
				List<DomainDto> certG3List = readMapper.selectG3Domain();

				for (DomainDto cert : certG3List) {
					// 「2020-12-18 00:00:00.0 yahoo.co.jp JPRS Domain Validation Authority - G3 株式会社日本レジストリサービス」 の形式でlogFile出力
					logFile.write(
							cert.getRecUpdDate() + "\t"
							+ cert.getDnCn() + "\t\t"
							+ cert.getStatus() + "\t\t"
							+ readMapper.selectAgentName(readMapper.selectJointAgentId(cert))
							+ "\r\n");
				}
				logFile.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

//	@Transactional
//	public void outputLogAll(String logFileName, String path) {
//
//			try (FileWriter logFile = new FileWriter(path + "all." + logFileName, true)) {
//
//				// 全数検査
//				List<DomainDto> certAllList =  readMapper.selectAllDomain();
//
//				for (DomainDto cert : certAllList) {
//					// 「2020-12-18 00:00:00.0 yahoo.co.jp JPRS Domain Validation Authority - G3」 の形式でlogFile出力
//					logFile.write(cert.getRecUpdDate() + "  " + cert.getDnCn() + "  " + cert.getStatus() + "\r\n");
//				}
//
//				logFile.flush();
//
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//	}


}
