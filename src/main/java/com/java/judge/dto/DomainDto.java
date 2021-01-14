package com.java.judge.dto;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.stereotype.Repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * DomainテーブルDTO
 */
@Entity
@Repository
@Table(name = "Domain")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DomainDto {

	@Id
	private String issueApplyId;

	private String dnCn;

	private String employeeName;

	private String Status;

	private Timestamp recUpdDate;

	private boolean wildcardFlag;

}