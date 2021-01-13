package com.java.judge.dto;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//CSVの各項目を読み込む順序を設定する
@JsonPropertyOrder({
	"issueApplyId",
	"jointAgentId",
	"certificateClass",
	"twoWayFlag",
	"paymentUnitClass",
	"validTermClass",
	"certificateStatus",
		//	11:審査中
		//	12:取下
		//	14:不承認
		//	15:承認
		//	20:発行待ち
		//	21:有効_更新期間前
		//	22:有効_更新期間中
		//	23:有効_更新済
		//	24:期限切れ
		//	25:失効
	"certificateX509",
	"certificateSerialNumber",
	"validTermStartDate",
	"validTermEndDate",
	"updatedApplyId",
	"revokeApplyId",
	"revokeReserveClass",
	"revokeDate",
	"reissueCertificateClass",
	"reissuedClass",
	"issueAvailableDate" })

@Entity
@Repository
@Table(name = "Certificate")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CertificateDto {

	@Id
	private String issueApplyId;

	private String jointAgentId;

	private String certificateClass;

	private String twoWayFlag;  //	#### 本当はboolean型だが、postgres特有の"t/f"で入っているためとりあえずchar型

	private String paymentUnitClass;

	private String validTermClass;

	private String certificateStatus;

	private String certificateX509;

	private String certificateSerialNumber;
//  Timesramp型でうまくいかないのでとりあえずString型で定義
//	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss", timezone="UTC")
//	private Timestamp validTermStartDate;
//	private Timestamp validTermEndDate;

	private String validTermStartDate;

	private String validTermEndDate;

	private String updatedApplyId;

	private String revokeApplyId;

	private String revokeReserveClass;

//	private Timestamp revokeDate;
	private String revokeDate;

	private String reissueCertificateClass;

	private String reissuedClass;

	private Date issueAvailableDate;

}