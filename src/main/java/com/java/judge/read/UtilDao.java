package com.java.judge.read;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.java.judge.dto.DomainDto;
import com.java.judge.mapper.ReadMapper;

@Component
public class UtilDao {

	@Autowired
	ReadMapper readMapper;

	/*
	 * DomainテーブルからG3証明書のdn_cnリストを取得
	 */
	@Transactional
	public List<DomainDto> getG3List() {

		List<DomainDto> g3DnCn = readMapper.selectG3Domain();

		return g3DnCn;
	}


	/*
	 * Domainテーブルからすべてのdn_cnリストを取得
	 */
	@Transactional
	public List<DomainDto> getAllList() {

		List<DomainDto> allDnCn = readMapper.selectAllDomain();

		return allDnCn;
	}


	/*
	 * Domainテーブルからワイルドカードのdn_cnリストを取得
	 */
	@Transactional
	public List<DomainDto> getWildcardList() {

		List<DomainDto> wildDnCn = readMapper.selectWildcardDomain();

		return wildDnCn;
	}


	/*
	 * Domainテーブルの更新
	 */
	@Transactional
	public void updateDomainTable(DomainDto updDomain) {
		readMapper.updateDomain(updDomain);
	}

}
