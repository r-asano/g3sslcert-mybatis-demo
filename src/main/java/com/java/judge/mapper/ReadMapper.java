package com.java.judge.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.java.judge.dto.AgentDto;
import com.java.judge.dto.CertificateDto;
import com.java.judge.dto.DomainDto;

/*
 * sslcertdbのマッパー
 */
@Mapper
public interface ReadMapper {

    // certificate Table
    void insertCertificate(CertificateDto certificate);

    List<String> selectX509();

    String selectIssueApplyId(String certificateX509);

    String selectJointAgentId(DomainDto domain);


    // domain Table
    void insertDomain(DomainDto domain);

    List<DomainDto> selectG3Domain();

    List<DomainDto> selectAllDomain();

    int countG3();

    int countDV();

    int countOV();

    DomainDto selectOneCert(String dnCn);

    void updateDomain(DomainDto domain);


    // agent Table
    void insertAgent(AgentDto agent);

    String selectAgentName(String jointAgentId);
}
