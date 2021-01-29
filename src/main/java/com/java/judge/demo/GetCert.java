package com.java.judge.demo;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.java.judge.read.UtilDaoInterface;

import sun.security.ssl.SSLSocketImpl;

@Service
public class GetCert {

    @Autowired
    private ReadMapper readMapper;

    @Autowired
    private DomainObjectSet object;

    @Autowired
    private UtilDaoInterface dao;

    @Value("${app.path}")
    private String path;

    @Value("${app.getCertPrefix}")
    private String getCertPrefix;

    @Value("${app.timeout}")
    private Integer timeout;

    @Value("${mail.encoding}")
    private String ENCODE;

    /*
     * 証明書情報の取得
     * 証明書の状態更新
     * ログの出力
     */
    @Transactional
    public void getCertIssuerStatus(List<DomainDto> domainList, String prefixAll, String dateString) throws IOException {


//        // プロキシの名前解決ができないのでIPで指定
//        SocketAddress addr = new InetSocketAddress("172.18.6.18", 8080);
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);

        // 実行ログ
        String getCertLogFile = prefixAll + getCertPrefix + dateString;

        // FileOutputStreamで文字コード・改行コードを指定(Shift-JIS,\r\n)
        PrintWriter writer = new PrintWriter(
                          new BufferedWriter(
                          new OutputStreamWriter(
                          new FileOutputStream
                            (path + getCertLogFile), ENCODE)));


        // getCertLog：検索日時、CN、検索ドメイン名、指定事業者名、検索結果(*)、SNI有効
        String headerRec =
                "Timestamp, dn_cn, FQDN, agent_name, status, SNI\r\n"
                + "------------------------------------------\r\n";
        writer.print(headerRec);

        // wildcard用にList型を用意
        List<String> dnCn = new ArrayList<String>();

        for (DomainDto domain : domainList) {

            // nullPointerExceptionの回避
            String status = "";

            System.out.println("================================================\r\n\r\n");
            System.out.println("対象CN: " + domain.getDnCn() + "\r\n\r\n");
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

                // SNI無効
                boolean disableSNI = true;
                boolean onemore = false;

                // 排他的論理和で判断
                while (disableSNI ^ onemore) {

                    // 実行TimeStamp
                    String updTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                            .format(new Timestamp(System.currentTimeMillis()));

                    try {
                        // 対象ドメイン名のHTTPS接続
                        URL destinationURL = new URL("https://" + cn);
                        HttpsURLConnection connection = (HttpsURLConnection) destinationURL.openConnection(); // proxy
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

                        // connectionのタイムアウト設定
                        connection.setConnectTimeout(timeout);

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
                                System.out.println("Certificate is active for current date, SNI: " + !disableSNI);

                                String issuer = ((X509Certificate) certs[0]).getIssuerX500Principal().getName();

                                // statusのみ抽出
                                int startStatus = issuer.indexOf("CN=") + 3;

                                if (issuer.contains(",")) {
                                    int endStatus = issuer.indexOf(",");
                                    status = issuer.substring(startStatus, endStatus);
                                } else {
                                    status = issuer.substring(startStatus);
                                }

                            } catch (CertificateExpiredException e) {
                                System.err.println("Certificate is expired: " + cn + ", SNI:" + !disableSNI);
                                status = "ERROR: " + e.getMessage();
                            }
                        } else {
                            System.err.println("Unknown certificate type: " + cn + ", SNI: " + !disableSNI);
                            status = "ERROR: UNKNOWN CERTIFICATE TYPE";
                        }
                    } catch (Exception e) {
                        System.err.println(e.toString() + ": " + cn + ", SNI: " + !disableSNI);
                        status = "ERROR: " + e.toString();
                    }

                    // getCertLog：検索日時、CN、検索ドメイン名、指定事業者名、検索結果(*)、SNI有効
                    String getCertLogRec = updTime + ","
                            + domain.getDnCn() + ","
                            + cn + ","
                            + readMapper.selectAgentName(readMapper.selectJointAgentId(domain)) + ","
                            + status + ","
                            + !disableSNI
                            + "\r\n";
                    writer.print(getCertLogRec);

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
            System.out.println("DOMAIN: " + domain.toString());
            System.out.println("=====================================");

            // DB更新
            dao.updateDomainTable(domain);

            // Delay
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(e.toString());
                e.getStackTrace();
            }
        }
        writer.flush();
        writer.close();
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
