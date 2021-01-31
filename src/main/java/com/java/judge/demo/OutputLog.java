package com.java.judge.demo;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.judge.dto.DomainDto;
import com.java.judge.mapper.ReadMapper;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
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

        log.info("outputG3Log 開始");

        String remainG3LogFile = prefixAll + remainG3Prefix + dateString;

        try {
            // FileOutputStreamで文字コード・改行コードを指定
            PrintWriter writer = new PrintWriter(
                              new BufferedWriter(
                              new OutputStreamWriter(
                              new FileOutputStream
                                (path + remainG3LogFile), ENCODE)));

            writer.print("rec_upd_date, dn_cn, status, agent_name" + "\r\n");
            writer.print("-----------------------------------------------------------------" + "\r\n");

            // statusがG3のレコードを抽出
            List<DomainDto> G3DomainList = readMapper.selectG3Domain();

            for (DomainDto domain : G3DomainList) {
                writer.print(
                                domain.getRecUpdDate() + ","
                                + domain.getDnCn() + ","
                                + domain.getStatus() + ","
                                + readMapper.selectAgentName(readMapper.selectJointAgentId(domain))
                                + "\r\n"
                                );
            }
            writer.flush();
            writer.close();
            log.info("outputG3Log 正常終了");
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.toString());
        }
    }
}
