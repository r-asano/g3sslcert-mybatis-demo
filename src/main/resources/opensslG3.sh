#!/bin/bash
## 引数で指定したCNの証明書をHTTP接続で取得、発行者のCNを抜き出す
openssl s_client -connect $1:443 < /dev/null 2> /dev/null | openssl x509 -issuer |
sed -e 's/.*CN=\([0-9a-zA-Z.*]*\)/\1/'