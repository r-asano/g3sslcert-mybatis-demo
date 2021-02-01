package com.java.judge.read;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.judge.dto.DomainDto;
import com.java.judge.mapper.ReadMapper;

/*
 * DAOクラス
 */
@Service
public class UtilDao implements UtilDaoInterface {

    @Autowired
    private ReadMapper readMapper;

    @Override
    public List<DomainDto> getG3List() {
        List<DomainDto> g3DnCn = readMapper.selectG3Domain();
        return g3DnCn;
    }

    @Override
    public List<DomainDto> getAllList() {
        List<DomainDto> allDnCn = readMapper.selectAllDomain();
        return allDnCn;
    }

    @Override
    // Updateに対してトランザクション処理をかける
    @Transactional
    public void updateDomainTable(DomainDto updDomain) {
        readMapper.updateDomain(updDomain);
    }

}
