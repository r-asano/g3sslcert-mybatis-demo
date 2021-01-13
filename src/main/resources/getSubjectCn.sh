#!/bin/bash
## 単一のX509証明書から申請者のCNを取得するスクリプト
## ファイル形式ではなく、Stringで渡しているためうまくいかない可能性がある
## 上手くいかなければechoの引数として渡し、-inオプションを取り除く
echo $1 | openssl x509 -subject -issuer -noout | sed 's/.*CN=\([0-9a-zA-Z.*]*\)/\1/'