package com.java.judge.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.java.judge.dto.AgentDto;
import com.java.judge.dto.CertificateDto;
import com.java.judge.dto.DomainDto;

/*
 * sslcertdbのマッパー
 */
@Mapper
public interface ReadMapper {

    // certificate Table
    @Insert("INSERT INTO "
            + "certificate "
            + "VALUES ("
            + "#{issueApplyId}, "
            + "#{jointAgentId}, "
            + "#{certificateClass},"
            + "#{twoWayFlag}, "
            + "#{paymentUnitClass}, "
            + "#{validTermClass}, "
            + "#{certificateStatus},"
            + "#{certificateX509}, "
            + "#{certificateSerialNumber}, "
            + "#{validTermStartDate}, "
            + "#{validTermEndDate},"
            + "#{updatedApplyId}, "
            + "#{revokeApplyId}, "
            + "#{revokeReserveClass}, "
            + "#{revokeDate},"
            + "#{reissueCertificateClass}, "
            + "#{reissuedClass}, "
            + "#{issueAvailableDate} )")
    void insertCertificate(CertificateDto certificate);

    @Select("SELECT certificate_x509 "
            + "FROM certificate "
            + "WHERE ("
            + "certificate_x509 IS NOT NULL "
            + "AND certificate_x509 !='') "
            + "AND (valid_term_start_date <= '2019-07-31' AND valid_term_end_date >= '2021-01-31' "
            + "OR valid_term_start_date BETWEEN '2019-08-01' AND '2019-09-30')")
    List<String> selectX509();

    @Select("SELECT issue_apply_id "
            + "FROM certificate "
            + "WHERE certificate_x509=#{certificateX509}")
    String selectIssueApplyId(String certificateX509);

    @Select("SELECT joint_agent_id FROM certificate WHERE issue_apply_id=#{issueApplyId}")
    String selectJointAgentId(DomainDto domain);


    // domain Table
    @Insert("INSERT INTO domain "
            + "VALUES ("
            + "#{issueApplyId}, "
            + "#{dnCn}, "
            + "#{employeeName}, "
            + "#{status}, "
            + "#{recUpdDate})")
    void insertDomain(DomainDto domain);

    @Select("SELECT * "
            + "FROM domain "
            + "WHERE ("
            + "status LIKE 'JPRS%' AND status LIKE '%G3') ")
    List<DomainDto> selectG3Domain();

    @Select("SELECT * "
            + "FROM domain")
    List<DomainDto> selectAllDomain();

    @Select("SELECT COUNT(*) "
            + "FROM domain "
            + "WHERE (status LIKE 'JPRS%' AND status LIKE '%G3')")
    int countG3();

    @Select("SELECT COUNT(*) "
            + "FROM domain "
            + "WHERE ("
            + "status LIKE 'JPRS%' "
            + "AND status LIKE '%Domain%' "
            + "AND status LIKE '%G3')")
    int countDV();

    @Select("SELECT COUNT(*) "
            + "FROM domain "
            + "WHERE ("
            + "status LIKE 'JPRS%' "
            + "AND status LIKE '%Organization%' "
            + "AND status LIKE '%G3')")
    int countOV();


    @Select("SELECT * "
            + "FROM domain "
            + "WHERE dn_cn=#{dnCn}")
    DomainDto selectOneCert(String dnCn);

    @Update("UPDATE domain "
            + "SET "
            + "status=#{status}, "
            + "rec_upd_date=#{recUpdDate} "
            + "WHERE issue_apply_id=#{issueApplyId}")
    void updateDomain(DomainDto domain);

    @MapKey("jointAgentId")
    @Select("SELECT joint_agent_id,count(*) "
            + "FROM domain "
            + "WHERE status LIKE 'JPRS%' AND status LIKE '%G3' "
            + "GROUP BY joint_agent_id "
            + "ORDER BY count(*) desc")
    Map<String, Integer> countG3GroupByAgent();



    // agent Table
    @Insert("INSERT INTO agent "
            + "VALUES ("
            + "#{jointAgentId}, "
            + "#{agentName})")
    void insertAgent(AgentDto agent);

    @Select("SELECT agent_name "
            + "FROM agent "
            + "WHERE joint_agent_id=#{jointAgentId}")
    String selectAgentName(String jointAgentId);

    @MapKey("jointAgentId")
    @Select("SELECT joint_agent_id,agent_name "
            + "FROM agent ")
    Map<String, String> selectAgentNameMap();
}
