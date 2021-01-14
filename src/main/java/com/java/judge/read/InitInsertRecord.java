package com.java.judge.read;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.java.judge.dto.AgentDto;
import com.java.judge.dto.AgentListDto;
import com.java.judge.dto.CertificateDto;
import com.java.judge.dto.DomainDto;
import com.java.judge.mapper.ReadMapper;


@Component
public class InitInsertRecord {

	@Autowired
	ReadMapper readMapper;
	@Autowired
	CertificateDto certificate;
	@Autowired
	DomainDto domain;
	@Autowired
	AgentDto agent;
	@Autowired
	AgentListDto agentList;


	/*
	 * Certificateテーブルの初期挿入
	 */
	@Transactional
	public void InsertCertificate() throws IOException {

		// TSVファイルを指定し順に読み出す
		CsvMapper mapper = new CsvMapper();
		// ヘッダあり、タブ区切り
		CsvSchema schema = mapper.schemaFor(CertificateDto.class).withHeader().withColumnSeparator('\t');
		String listPath = "C:/common/開発部研修/kaihatsubuKenshu/certificate_list/certificate_list.csv";
		Path path = Paths.get(listPath);

		try ( BufferedReader br = Files.newBufferedReader(path) ) {
			MappingIterator<CertificateDto> it = mapper.readerFor(CertificateDto.class).with(schema).readValues(br);

			// 試しに3件のみ登録
//			for (int i = 0; i < 10; i++) {
			while (it.hasNextValue()) {
				// TSVファイルを順に呼び出す
				certificate = it.nextValue();

				// CertificateテーブルにInsert
				readMapper.insertCertificate(certificate);
			}
		}
	}


	/*
	 * Domainテーブルの初期挿入
	 */
	@Transactional
	public void InsertDomain() throws InterruptedException {

		// X509証明書の取得
		List<String> x509List =  readMapper.selectX509();


		for (String x509: x509List) {

			try (ByteArrayInputStream is = new ByteArrayInputStream(x509.getBytes())){

				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				Certificate cert = cf.generateCertificate(is);

				// Issuer: CN=JPRS Organization Validation Authority - G2, O="Japan Registry Services Co., Ltd.", C=JP
				// getName => CN=JPRS Organization Validation Authority - G2,O=Japan Registry Services Co.\, Ltd.,C=JP
				X500Principal subject = ((X509Certificate) cert).getSubjectX500Principal();
				X500Principal issuer = ((X509Certificate) cert).getIssuerX500Principal();

				// Domainオブジェクトにdn_cnをセット
				int startCn = subject.getName().indexOf("CN=")+3;

				if (subject.getName().contains(",")) {
					int endCn = subject.getName().indexOf(",");
					String dnCn = subject.getName().substring(startCn, endCn);
					domain.setDnCn(dnCn);
				} else {
					String dnCn = subject.getName().substring(startCn);
					domain.setDnCn(dnCn);
				}

				// "JPRS Domain Validation Authority - G1"(fullStatus)のような文字列になっている
				// => CN=Amazon などに対応するためにCN全文に変更

				// Domainオブジェクトにdn_cnをセット
				int startStatus = issuer.getName().indexOf("CN=")+3;

				if (issuer.getName().contains(",")) {
					int endStatus = issuer.getName().indexOf(",");
					String status = issuer.getName().substring(startStatus, endStatus);
					domain.setStatus(status);
				} else {
					String status = issuer.getName().substring(startStatus);
					domain.setDnCn(status);
				}

				// 残りのissue_apply_id, employee_name, rec_upd_dateをセット
				domain.setIssueApplyId(readMapper.selectIssueApplyId(x509));
				domain.setEmployeeName("asano");
				Timestamp updDate = new Timestamp(System.currentTimeMillis());
				domain.setRecUpdDate(updDate);

				if( domain.getDnCn().startsWith("*")) {
					domain.setWildcardFlag(true);
				} else {
					domain.setWildcardFlag(false);
				}

				System.out.println(subject.toString());
				System.out.println(issuer.toString());
				System.out.println("issue_apply_id: " + domain.getIssueApplyId());
				System.out.println("dn_cn: " + domain.getDnCn());
				System.out.println("employee_name: " + domain.getEmployeeName());
				System.out.println("status: " + domain.getStatus());
				System.out.println("rec_upd_date: " + domain.getRecUpdDate());
				System.out.println("wildcard_flag:" + domain.isWildcardFlag());

				// Domainオブジェクトを登録
				readMapper.insertDomain(domain);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	/*
	 * Agentテーブルの初期挿入
	 */
	@Transactional
	public void InsertAgent() throws IOException {

		// TSVファイルを指定し順に読み出す
		CsvMapper mapper = new CsvMapper();
		// ヘッダあり、タブ区切り
		CsvSchema schema = mapper.schemaFor(AgentListDto.class).withHeader().withColumnSeparator('\t');
		String listPath = "C:/common/開発部研修/kaihatsubuKenshu/agent_list/agent_list.csv";
		Path path = Paths.get(listPath);

		try ( BufferedReader br = Files.newBufferedReader(path) ) {
			MappingIterator<AgentListDto> it = mapper.readerFor(AgentListDto.class).with(schema).readValues(br);

			while (it.hasNextValue()) {
				// TSVファイルを順に呼び出す
				agentList = it.nextValue();
				agent.setJointAgentId(agentList.getJointAgentId());
				agent.setAgentName(agentList.getAgentName());

				// CertificateテーブルにInsert
				readMapper.insertAgent(agent);
			}
		}
	}


	/*
	 * Domainテーブルの初期挿入( shellを使用 )
	 */
//	@Transactional
//	public void InsertDomainShell() throws InterruptedException {
//
//		String sh = "C:/Eclips4.6/pleiades_2019/workspace/g3sslcert-mybatis-demo/src/main/resources/getSubjectCn.sh";
//
//		// X509証明書の取得
//		List<String> x509List =  readMapper.selectX509();
//
//
//		try {
//
//			for (String x509: x509List) {
//
//				// 証明書を引数として指定して申請者のCN（1行目）、Status（2行目）を出力
//				ProcessBuilder pb = new ProcessBuilder(sh, x509);
//				Process p = pb.start();
//
//				p.waitFor();
//
//				// Domainオブジェクトにdn_cnをセット
//				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//				domain.setDnCn(br.readLine());
//
//				// Statusは工夫が必要。"JPRS Domain Validation Authority - G1"(fullStatus)のような文字列になっているため、
//				// ハイフン以降の文字列(status)を取得する(空白を考えると+2かも)
//				String fullStatus = br.readLine();
//				String status = fullStatus.substring(fullStatus.indexOf("-")+2);
//
//				// Domainオブジェクトにstatusをセット
//				domain.setStatus(status);
//
//				// 残りのissue_apply_id, employee_name, rec_upd_dateをセット
//				domain.setIssueApplyId(readMapper.selectIssueApplyId(x509));
//				domain.setEmployeeName("asano");
//				Timestamp updDate = new Timestamp(System.currentTimeMillis());
//				domain.setRecUpdDate(updDate);
//
//				if( domain.getDnCn().startsWith("*")) {
//					domain.setWildcardFlag(true);
//				} else {
//					domain.setWildcardFlag(false);
//				}
//
//				// Domainオブジェクトを登録
//
//				readMapper.insertDomain(domain);
//
//				p.destroy();
//			}
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
