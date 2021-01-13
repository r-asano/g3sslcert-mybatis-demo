---- テーブルの初期化
--DROP TABLE IF EXISTS Certificate;
--DROP TABLE IF EXISTS Domain;
--DROP TABLE IF EXISTS Agent;
--DROP TABLE IF EXISTS Wildcard;

-- "テーブル内に"踏めることができる有効最大長は65535 byte ÷ 文字のbyte数（UTF-8なら最大3 byte）
CREATE TABLE IF NOT EXISTS Certificate (
	issue_apply_id varchar(12) NOT NULL,
	joint_agent_id varchar(9),
	-- certificate_class 1:DV証明書 2:OV証明書
	certificate_class char(1),
	-- two_way_flag ダブルアドレスオプションの有無
	-- #### 本当はboolean型だが、postgres特有の"t/f"で入っているためとりあえずchar型
	two_way_flag char(3),
	-- payment_unit_class 1:月払い 2:一括払い
	payment_unit_class char(1),
	-- valid_term_class 1:1年 2:2年 3:3年
	valid_term_class char(1),
	-- certificate_status 11:審査中 など
	certificate_status char(2),
	-- certificate_x509 証明書、1500 bytes程度ある
	certificate_x509 TEXT,
	certificate_serial_number varchar(49),
--  Timestamp型でうまくいかないのでとりあえずString型で定義
--	valid_term_start_date TIMESTAMP,
--	valid_term_end_date TIMESTAMP,
	valid_term_start_date varchar(255),
	valid_term_end_date varchar(255),
	updated_apply_id varchar(12),
	revoke_apply_id varchar(12),
	-- revoke_reserve_class 0:なし 1:月末失効
	revoke_reserve_class char(1),
--	revoke_date TIMESTAMP,
	revoke_date varchar(255),
	-- reissue_certificate_class 0:なし 1:新規証明書再発行 2:更新証明書再発行
	reissue_certificate_class char(1),
	-- reissue_class 1:新規発行 2:再発行済
	reissued_class char(1),
	-- issue_available_date 新規、再発行の場合には申請日を設定する。更新の場合には更新元証明書の有効期間終了日の月初日を設定する。
	issue_available_date DATE,
	PRIMARY KEY (issue_apply_id)
);

CREATE TABLE IF NOT EXISTS Agent (
	joint_agent_id varchar(9) NOT NULL,
	agent_name varchar(180) NOT NULL,
	PRIMARY KEY (joint_agent_id)
);

CREATE TABLE IF NOT EXISTS Domain (
	issue_apply_id varchar(12) NOT NULL,
	dn_cn varchar(180) NOT NULL,
	employee_name varchar(47) NOT NULL,
	status varchar(180) NOT NULL,
	rec_upd_date TIMESTAMP NOT NULL,
	wildcard_flag boolean NOT NULL,
	PRIMARY KEY (issue_apply_id)
);

--CREATE TABLE IF NOT EXISTS Wildcard (
--	dn_cn varchar(180) NOT NULL,
--	dn varchar(180) NOT NULL,
--	PRIMARY KEY (dn_cn)
--);
