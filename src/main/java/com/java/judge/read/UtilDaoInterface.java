package com.java.judge.read;

import java.util.List;

import org.springframework.stereotype.Service;

import com.java.judge.dto.DomainDto;

/*
 * DAOクラス
 */
@Service
public interface UtilDaoInterface {

    // DomainテーブルからG3証明書のdn_cnリストを取得
    public List<DomainDto> getG3List();

    // Domainテーブルからすべてのdn_cnリストを取得
    public List<DomainDto> getAllList();

    // Domainテーブルの更新
    public void updateDomainTable(DomainDto updDomain);


}
