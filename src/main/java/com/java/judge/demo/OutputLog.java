package com.java.judge.demo;

import java.io.FileWriter;
import java.io.IOException;
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
    private ReadMapper readMapper;

    @Value("${app.path}")
    private String path;

    @Value("${app.logPrefixSSL}")
    private String prefixSSL;

    @Value("${mail.encoding}")
    private String ENCODE;

    /*
     * 残存G3証明書ログの出力
     */
    @Transactional
    public void outputG3Log(String dateString) {

        String logFileName = prefixSSL + dateString;

        try (FileWriter writer = new FileWriter(path + logFileName)) {

            writer.write("CHECK_DATE : CN :  : AGENT" + "\r\n");
            writer.write("-----------------------------------------------------------------" + "\r\n");

            // statusがG3のレコードを抽出
            List<DomainDto> G3DomainList = readMapper.selectG3Domain();

            for (DomainDto domain : G3DomainList) {
                writer.write(
                        domain.getRecUpdDate() + ":"
                                + domain.getDnCn() + ":"
                                + domain.getStatus() + ":"
                                + readMapper.selectAgentName(readMapper.selectJointAgentId(domain))
                                + "\r\n");
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
