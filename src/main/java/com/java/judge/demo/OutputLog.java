package com.java.judge.demo;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.judge.dto.DomainDto;
import com.java.judge.mapper.ReadMapper;

@Service
public class OutputLog {

    @Autowired
    ReadMapper readMapper;

    @Value("${app.path}")
    private String path;

    @Value("${app.logFilePrefix}")
    private String prefix;

    /*
     * 残存G3証明書ログの出力
     */
    @Transactional
    public void outputG3Log() {

        String logFileName = prefix + new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        try (FileWriter logFile = new FileWriter(path + logFileName)) {

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
}
