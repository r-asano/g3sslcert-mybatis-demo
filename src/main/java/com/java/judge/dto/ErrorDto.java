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
 * ErrorテーブルDTO
 */
@Entity
@Repository
@Table(name = "error")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ErrorDto {

    @Id
    private String issueApplyId;

    private String dnCn;

    private String error;

    private Timestamp recUpdDate;

}