package com.java.judge.demo;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.java.judge.dto.DomainDto;

@Component
public class DomainObjectSet {

    @Value("{app.employeeName}")
    private String employeeName;

    /*
     * Domainオブジェクトの設定
     */
    public DomainDto domainObjectSet(DomainDto domain, String status) {

        domain.setIssueApplyId(domain.getIssueApplyId());
        domain.setDnCn(domain.getDnCn());
        domain.setEmployeeName(employeeName);
        domain.setStatus(status);
        Timestamp updDate = new Timestamp(System.currentTimeMillis());
        domain.setRecUpdDate(updDate);

        System.out.println("=====================================");
        System.out.println("OBJECT: " + domain.toString());
        System.out.println("=====================================");

        return domain;

    }
}
