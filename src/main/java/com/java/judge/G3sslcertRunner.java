package com.java.judge;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.java.judge.demo.GetCert;
import com.java.judge.demo.OutputLog;
import com.java.judge.dto.DomainDto;
import com.java.judge.mail.SendMail;
import com.java.judge.read.UtilDao;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class G3sslcertRunner implements ApplicationRunner {

    @Autowired
    private UtilDao dao;

    @Autowired
    private GetCert getCert;

    @Autowired
    private OutputLog output;

    @Autowired
    private SendMail mail;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        log.info("G3sslcertApplication 開始");

        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        // 初日と最終日のみ全数検査
        boolean zensu = false;
        if (dateString.equals("2021-01-25") || dateString.equals("2021-03-31")) {
            zensu = true;
        }

        log.info("全数検査モード: " + zensu);

        String prefixAll;
        List<DomainDto> domainList;
        if (zensu) {
            domainList = dao.getAllList();
            prefixAll = "all_";
        } else {
            domainList = dao.getG3List();
            prefixAll = "";
        }

        // dn_cnリストの取得 + DB更新
        getCert.getCertIssuerStatus(domainList, prefixAll, dateString);
        // 残存G3ログ出力
        output.outputG3Log(dateString, prefixAll);
        // メールの送出
        mail.sendMail(domainList.size(), prefixAll, dateString);

        log.info("G3sslcertApplication 正常終了");
    }

}