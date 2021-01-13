package com.java.judge.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.java.judge.dto.AgentDto;
import com.java.judge.dto.CertificateDto;
import com.java.judge.dto.DomainDto;

@Mapper
public interface JudgeMapper {

	/*
	 * Certificate Entity
	 */
	@Select("SELECT * FROM Certificate WHERE csr_id=#{csrId}")
	CertificateDto selectCert(CertificateDto certificate);




	/*
	 * Domain Entity
	 */

	@Select("SELECT * FROM Domain")
	List<DomainDto> selectAllDomain();

	// bfG2は今回は調査対象外
	@Select("SELECT * FROM Domain WHERE status='G3'")
	List<DomainDto> selectCertG3();

	@Update("UPDATE Domain SET status='G4', WHERE dn_cn= #{dnCn}")
	void updateStatusToG4(DomainDto domain);

	@Select("SELECT COUNT(*) FROM Domain WHERE status='G3'")
	int countG3();

	@Select("SELECT wildcard_frag FROM Domain")
	boolean wildcardFlag();


//    @Insert("INSERT INTO certificate (todo_title, finished, created_at) VALUES (#{todoTitle}, #{finished}, #{createdAt})")
//    @Options(useGeneratedKeys = true, keyProperty = "certId") // (3)
//    void insert(Certificate certificate);
//

	/*
	 *  Agent Entity
	 */

	@Select("SELECT * FROM Agent")
	List<AgentDto> selectAllAgent();

	@Select("SELECT * FROM Agent WHERE agent_id = #{agentId}")
	AgentDto selectAgent(AgentDto agent); // Domainのagent_idの方が使いやすいかも


	/*
	 *  WildCard Entity
	 */

	@Select("SELECT fqdn FROM WildCard WHERE dn_cn= #{dnCn}")
	String Fqdn(DomainDto domain);


}
