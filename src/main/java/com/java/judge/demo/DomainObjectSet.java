package com.java.judge.demo;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.java.judge.dto.DomainDto;

@Component
public class DomainObjectSet {

    @Autowired
    private DomainDto domain;

    /*
     * Domainオブジェクトの設定
     */
    public void domainObjectSet(DomainDto domain, String status) {

        domain.setIssueApplyId(domain.getIssueApplyId());
        domain.setDnCn(domain.getDnCn());
        domain.setEmployeeName("asano");
        domain.setStatus(status);
        Timestamp updDate = new Timestamp(System.currentTimeMillis());
        domain.setRecUpdDate(updDate);

    }
}
