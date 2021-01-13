package com.java.judge.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.java.judge.dto.AgentDto;
import com.java.judge.dto.CertificateDto;
import com.java.judge.dto.DomainDto;
import com.java.judge.dto.WildcardDto;


@Mapper
public interface ReadMapper {
	/*
	 * Certificate Table
	 */
	@Insert("INSERT INTO Certificate VALUES (#{issueApplyId}, #{jointAgentId}, #{certificateClass},"
			+ "#{twoWayFlag}, #{paymentUnitClass}, #{validTermClass}, #{certificateStatus},"
			+ "#{certificateX509}, #{certificateSerialNumber}, #{validTermStartDate}, #{validTermEndDate},"
			+ "#{updatedApplyId}, #{revokeApplyId}, #{revokeReserveClass}, #{revokeDate},"
			+ "#{reissueCertificateClass}, #{reissuedClass}, #{issueAvailableDate} )")
	void insertCertificate(CertificateDto certificate);

	@Select("SELECT certificate_x509 FROM Certificate WHERE (certificate_x509 IS NOT NULL AND certificate_x509 !='') "
			+ "AND (valid_term_start_date <= '2019-07-31' AND valid_term_end_date >= '2021-01-01' "
			+ "OR valid_term_start_date BETWEEN '2019-08-01' AND '2019-09-30')")
	List<String> selectX509();

	@Select("SELECT issue_apply_id FROM Certificate WHERE certificate_x509=#{certificateX509}")
	String selectIssueApplyId(String certificateX509);

	@Select("SELECT joint_agent_id FROM Certificate WHERE issue_apply_id=#{issueApplyId}")
	String selectJointAgentId(DomainDto domain);



	/*
	 * Domain Table
	 */
	@Insert("INSERT INTO Domain VALUES (#{issueApplyId}, #{dnCn}, #{employeeName},"
			+ "#{status}, #{recUpdDate}, #{wildcardFlag})")
	void insertDomain(DomainDto domain);

	@Select("SELECT * FROM Domain WHERE (status LIKE 'JPRS%' AND status LIKE '%G3') ")
	List<DomainDto> selectG3Domain();

	@Select("SELECT * FROM Domain")
	List<DomainDto> selectAllDomain();

	@Select("SELECT * FROM Domain WHERE dn_cn LIKE '*%' ")
	List<DomainDto> selectWildcardDomain();

	@Update("UPDATE Domain SET status=#{status}, rec_upd_date=#{recUpdDate} WHERE issue_apply_id=#{issueApplyId}")
	void updateDomain(DomainDto domain);

	@Select("SELECT COUNT(*) FROM Domain WHERE (status LIKE 'JPRS%' AND status LIKE '%G3')")
	int countG3();

	@Select("SELECT COUNT(*) FROM Domain WHERE (status LIKE 'JPRS%' AND status LIKE '%Domain%' AND status LIKE '%G3')")
	int countDV();

	@Select("SELECT COUNT(*) FROM Domain WHERE (status LIKE 'JPRS%' AND status LIKE '%Organization%' AND status LIKE '%G3')")
	int countOV();





	/*
	 * Agent Table
	 */
	@Insert("INSERT INTO Agent VALUES (#{jointAgentId}, #{agentName})")
	void insertAgent(AgentDto agent);

	@Select("SELECT agent_name FROM Agent WHERE joint_agent_id=#{jointAgentId}")
	String selectAgentName(String jointAgentId);


	/*
	 * Wildcard Table
	 */
	@Insert("INSERT INTO Wildcard VALUES (#{dnCn}, #{dn})")
	void insertWildCard(WildcardDto wildcard);

	@Select("SELECT dn FROM Wildcard WHERE dn_cn=#{dnCn}")
	String selectNoWildcardDn(DomainDto domain);
}
