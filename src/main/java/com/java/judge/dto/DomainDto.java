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
 * 「インメモリのDB」が接続対象のデータベースとして指定されている場合、
 * アプリケーション実行時に、@Entity アノテーションをつけたクラスのテーブルを作成
 */

/*
 * DomainテーブルDTO
 */
@Entity
@Repository
@Table(name = "domain")
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

    private String jointAgentId;

    private boolean twoWayFlag;

}