package com.java.judge.dto;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.stereotype.Repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * AgentテーブルDTO
 */
@Entity
@Repository
@Table(name = "agent")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AgentDto {

    @Id
    private String jointAgentId;

    private String agentName;

}