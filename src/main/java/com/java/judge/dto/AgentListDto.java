package com.java.judge.dto;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@JsonPropertyOrder({
		"jointAgentId",
		"dojpAgentId",
		"tieAgentId",
		"agentName",
		"agentNameHiragana" })

@Entity
@Repository
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AgentListDto {

		/*
		 * Agentテーブルを挿入するための一時的なクラス
		 */

		@Id
		private String jointAgentId;

		private String dojpAgentId;
		private String tieAgentId;
		private String agentName;
		private String agentNameHiragana;

}