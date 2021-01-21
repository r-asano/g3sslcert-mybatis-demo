package com.java.judge;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.java.judge.demo.GetCert;
import com.java.judge.demo.OutputLog;
import com.java.judge.dto.DomainDto;
import com.java.judge.mail.SendMail;
import com.java.judge.read.UtilDao;

@SpringBootApplication
@MapperScan(basePackages = "com.java.judge.mapper")
public class G3sslcertMyBatisDemoApplication {

    public static void main(String[] args) throws Exception  {

//        ConfigurableApplicationContext context =
//                SpringApplication.run(G3sslcertMyBatisDemoApplication.class, args);
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("applicationContext.xml");

        UtilDao dao = context.getBean(UtilDao.class);
        GetCert getCert = context.getBean(GetCert.class);
        OutputLog output = context.getBean(OutputLog.class);
        SendMail mail = context.getBean(SendMail.class);
        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        // 初日と最終日のみ全数検査
        boolean zensu = false;
        if (dateString == "2021-01-21" || dateString == "2021-03-31") {
            zensu = true;
        }

        zensu = true;

        List<DomainDto> domainList;
        if (zensu) {
            domainList = dao.getAllList();
        } else {
            domainList = dao.getG3List();
        }

        // dn_cnリストの取得 + DB更新
        getCert.getCertIssuerStatus(domainList);
        // ログファイル出力
        output.outputG3Log();
        // メールの送出
        mail.sendMail(domainList.size());

        context.close();
    }

}
