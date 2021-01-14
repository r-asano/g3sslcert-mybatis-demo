package com.java.judge.read;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.java.judge.dto.CertificateDto;
import com.java.judge.mapper.ReadMapper;


@Component
public class ReadTSV {

	@Autowired
	ReadMapper readMapper;

	@Autowired
	CertificateDto certificate;


	/*
	 * TSVファイルからX509証明書情報のみを抽出
	 */
	public void extractX509() throws IOException {
		CsvMapper mapper = new CsvMapper();
		// ヘッダあり、タブ区切り
		CsvSchema schema = mapper.schemaFor(CertificateDto.class).withHeader().withColumnSeparator('\t');
		String output = "certificate_output.pem";
		String listPath = "C:/common/開発部研修/kaihatsubuKenshu/certificate_list/certificate_list.csv";

		Path path = Paths.get(listPath);
		try (
			BufferedReader br = Files.newBufferedReader(path);
			PrintWriter outputLine = new PrintWriter(new FileWriter(output))) {

				MappingIterator<CertificateDto> it = mapper.readerFor(CertificateDto.class).with(schema).readValues(br);

				// TSVファイルを1行ずつ読み込む
				while (it.hasNextValue()) {
				certificate = it.nextValue();
				System.out.println(certificate.getIssueApplyId());
				}
				outputLine.flush();
			}
	}
}
