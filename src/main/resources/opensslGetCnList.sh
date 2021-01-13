#!/bin/bash
## X509証明書からcn_list.txtに出力するスクリプト
openssl crl2pkcs7 -nocrl -certfile certificate_output.pem | openssl pkcs7 -print_certs -subject -noout | sed 's/.*CN=\([0-9a-zA-Z.*]*\)/\1/' > cn_list.txt