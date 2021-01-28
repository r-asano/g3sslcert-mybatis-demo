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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OutputLog {

    @Autowired
    private ReadMapper readMapper;

    @Value("${app.path}")
    private String path;

    @Value("${app.remainG3Prefix}")
    private String remainG3Prefix;

    @Value("${mail.encoding}")
    private String ENCODE;

    /*
     * 残存G3証明書ログの出力
     */
    @Transactional
    public void outputG3Log(String dateString, String prefixAll) {

        String logFileName = prefixAll + remainG3Prefix + dateString;

        try (FileWriter writer = new FileWriter(path + logFileName)) {

            writer.write("rec_upd_date, dn_cn,status, agent_name" + "\r\n");
            writer.write("-----------------------------------------------------------------" + "\r\n");

            // statusがG3のレコードを抽出
            List<DomainDto> G3DomainList = readMapper.selectG3Domain();

            for (DomainDto domain : G3DomainList) {
                writer.write(
                        domain.getRecUpdDate() + ","
                                + domain.getDnCn() + ","
                                + domain.getStatus() + ","
                                + readMapper.selectAgentName(readMapper.selectJointAgentId(domain))
                                + "\r\n");
                log.info(domain.toString());
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            log.info(e.toString());
        }
    }
}
