package com.java.judge.demo;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.judge.dto.DomainDto;
import com.java.judge.mapper.ReadMapper;
import com.java.judge.read.UtilDao;

import sun.security.ssl.SSLSocketImpl;

@Service
public class GetCert {

    @Autowired
    ReadMapper readMapper;

    @Autowired
    DomainDto domain;

    @Autowired
    DomainObjectSet object;

    @Autowired
    UtilDao dao;

    @Value("${app.path}")
    private String path;

    @Value("${app.logFilePrefix}")
    private String prefix;

    /*
     * 証明書情報の取得
     * 証明書の状態更新
     * エラーログの出力
     */
    @Transactional
    public void getCertIssuerStatus(List<DomainDto> domainList)
            throws KeyManagementException, NoSuchAlgorithmException, IOException, CertificateNotYetValidException,
            InterruptedException {

        String logFileName = prefix + new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        // エラーログファイル
        FileWriter getCertLogFile = new FileWriter(path + "getCert." + logFileName);

        // wildcard用にList型を用意
        List<String> dnCn = new ArrayList<String>();

        for (DomainDto domain : domainList) {

            // nullPointerExceptionの回避
            String status = "";

            System.out.println("================================================\r\n\r\n");
            System.out.println("対象ドメイン: " + domain.getDnCn() + "\r\n\r\n");
            System.out.println("================================================\r\n\r\n");

            // wildcardの判定
            dnCn.clear();
            if (domain.getDnCn().startsWith("*")) {
                dnCn.add(domain.getDnCn().substring(2));
                dnCn.add("www." + domain.getDnCn().substring(2));
            } else {
                dnCn.add(domain.getDnCn());
            }

            // wildcardなら2件のループ
            for (String cn : dnCn) {

                // SNI有効
                boolean disableSNI = false;
                boolean onemore = false;

                // 排他的論理和で判断
                while (!(disableSNI ^ onemore)) {

                    try {
                        URL destinationURL = new URL("https://" + cn);
                        HttpsURLConnection connection = (HttpsURLConnection) destinationURL.openConnection(); // 引数にproxy
                        connection.setRequestMethod("GET");

                        // 証明書チェック無効化
                        SSLContext sslContext = SSLContext.getInstance("SSL");
                        sslContext.init(null, new X509TrustManager[] { new RelaxedX509TrustManager() },
                                new SecureRandom());
                        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

                        // SNI無効化
                        if (disableSNI) {
                            socketFactory = new SNIDisabledSSLSocketFactory(socketFactory);
                        }

                        // connectionのタイムアウトを1000msに設定
                        connection.setConnectTimeout(1000);
                        connection.setReadTimeout(1000);

                        // 設定をconnectionに反映
                        connection.setSSLSocketFactory(socketFactory);

                        // connectionの確立
                        connection.connect();

                        System.out.println("==================== 接続成功 ==================");

                        // 対象CNの証明書情報取得
                        Certificate[] certs = connection.getServerCertificates();

                        // Statusの更新
                        if (certs[0] instanceof X509Certificate) {
                            try {
                                // 証明書が有効か判定
                                ((X509Certificate) certs[0]).checkValidity();
                                System.out.println("Certificate is active for current date");

                                String issuer = ((X509Certificate) certs[0]).getIssuerX500Principal().getName();

                                // statusのみ抽出
                                int startStatus = issuer.indexOf("CN=") + 3;

                                if (issuer.contains(",")) {
                                    int endStatus = issuer.indexOf(",");
                                    status = issuer.substring(startStatus, endStatus);
                                } else {
                                    status = issuer.substring(startStatus);
                                }

                                getCertLogFile.write("Get Cert Success! cn: " + cn + ": status: " + status + ", SNI: "
                                        + !disableSNI + "\r\n");

                            } catch (CertificateExpiredException e) {
                                System.err.println("Certificate is expired: " + cn);
                                getCertLogFile
                                        .write("Certificate is expired: " + cn + ", SNI: " + !disableSNI + "\r\n");
                                status = "ERROR: " + e.getMessage();
                            }
                        } else {
                            System.err.println("Unknown certificate type: " + cn + ", SNI: " + !disableSNI);
                            getCertLogFile.write(
                                    "Unknown certificate type:             " + cn + ", SNI: " + !disableSNI + "\r\n");
                            status = "ERROR: UNKNOWN CERTIFICATE TYPE, SNI: " + !disableSNI;
                        }
                    } catch (Exception e) {
                        System.err.println(e.toString() + ": " + cn + ", SNI: " + !disableSNI);
                        getCertLogFile
                                .write(e.toString() + ":                    " + cn + ", SNI: " + !disableSNI + "\r\n");
                        status = "ERROR: " + e.toString();
                    }

                    // SNIのいずれかにG3証明書がある場合、statusをG3とする（SNIありのログはとれない可能性あり）
                    if (status.contains("G3") && status.contains("JPRS")) {
                        break;
                    }

                    disableSNI = !disableSNI;
                    onemore = true;

                }

                // {dn_cn},www.{dn_cn}いずれかにG3証明書がある場合、statusをG3とする（wwwありのログはとれない可能性あり）
                if (status.contains("G3") && status.contains("JPRS")) {
                    break;
                }

            }

            // Domainオブジェクトにstausをセット
            object.domainObjectSet(domain, status);

            System.out.println("=====================================");
            System.out.println("Status: " + domain.getStatus());
            System.out.println("=====================================");

            // DB更新
            dao.updateDomainTable(domain);

            // Delay
            Thread.sleep(1000);
        }

        getCertLogFile.flush();
        getCertLogFile.close();
    }
}

/**
 * 全てを許可するTrustManager
 */
class RelaxedX509TrustManager implements X509TrustManager {
    public boolean isClientTrusted(X509Certificate[] chain) {
        return true;
    }

    public boolean isServerTrusted(X509Certificate[] chain) {
        return true;
    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }
}

/**
 * jsse.enableSNIExtensionの設定に関わらずSNIを無効にするSSLSocketFactory
 */
class SNIDisabledSSLSocketFactory extends SSLSocketFactory {
    private SSLSocketFactory baseSocketFactory;

    public SNIDisabledSSLSocketFactory(SSLSocketFactory baseSocketFactory) {
        this.baseSocketFactory = baseSocketFactory;
    }

    private Socket setSNI(Socket socket) {
        SSLParameters params = ((SSLSocketImpl) socket).getSSLParameters();
        params.setServerNames(new ArrayList<>()); // ホスト名を空にすることでSNIを無効にする
        ((SSLSocketImpl) socket).setSSLParameters(params);
        return socket;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return baseSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return baseSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket paramSocket, String paramString, int paramInt, boolean paramBoolean)
            throws IOException {
        return setSNI(baseSocketFactory.createSocket(paramSocket, paramString, paramInt, paramBoolean));
    }

    @Override
    public Socket createSocket(String paramString, int paramInt) throws IOException, UnknownHostException {
        return setSNI(baseSocketFactory.createSocket(paramString, paramInt));
    }

    @Override
    public Socket createSocket(String paramString, int paramInt1, InetAddress paramInetAddress, int paramInt2)
            throws IOException, UnknownHostException {
        return setSNI(baseSocketFactory.createSocket(paramString, paramInt1, paramInetAddress, paramInt2));
    }

    @Override
    public Socket createSocket(InetAddress paramInetAddress, int paramInt) throws IOException {
        return setSNI(baseSocketFactory.createSocket(paramInetAddress, paramInt));
    }

    @Override
    public Socket createSocket(InetAddress paramInetAddress1, int paramInt1, InetAddress paramInetAddress2,
            int paramInt2) throws IOException {
        return setSNI(baseSocketFactory.createSocket(paramInetAddress1, paramInt1, paramInetAddress2, paramInt2));
    }
}
