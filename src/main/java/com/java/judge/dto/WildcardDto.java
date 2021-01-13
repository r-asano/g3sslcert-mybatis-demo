package com.java.judge.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.stereotype.Repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Repository
@Table(name = "WildCard")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WildcardDto {

	@Id
	private String dnCn;

	private String dn;

}