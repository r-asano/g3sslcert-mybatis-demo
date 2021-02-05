package com.java.judge.demo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.java.judge.dto.DomainDto;
import com.java.judge.mapper.ReadMapper;
import com.java.judge.read.UtilDaoInterface;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import sun.security.ssl.SSLSocketImpl;

//finalフィールドへ値をセットするための引数付きコンストラクタを自動生成するLomBokアノテーション
@RequiredArgsConstructor
@Service
@Log4j2
public class GetCert {

    private final ReadMapper readMapper;

    private final DomainObjectSet object;

    private final UtilDaoInterface dao;

    @Value("${app.path}")
    private String path;

    @Value("${connection.timeout}")
    private Integer timeout;

    @Value("${mail.encoding}")
    private String ENCODE;

    @Value("${enviroment.profile}")
    String PROFILE;

    @Value("${delay.time}")
    private Integer delay;

    private HttpsURLConnection connection;

    /*
     * サーバー証明書情報の取得・状態更新
     */
    public void getCertIssuerStatus(List<DomainDto> domainList, String prefixAll, String dateString)
            throws IOException {

        log.info("GetCert 開始");

        // wildcard用にList型を用意
        List<String> dnCn = new ArrayList<String>();

        for (DomainDto domain : domainList) {

            // nullPointerExceptionの回避
            String status = "";

            log.info("検査対象コモンネーム: " + domain.getDnCn());

            // wildcardの判定
            dnCn.clear();
            if (domain.getDnCn().startsWith("*")) {
                dnCn.add(domain.getDnCn().substring(2));
                dnCn.add("www." + domain.getDnCn().substring(2));
                log.info("ワイルドカード証明書");
            } else if (domain.isTwoWayFlag()) {
                if(domain.getDnCn().startsWith("www")) {
                    dnCn.add(domain.getDnCn().substring(4));
                    dnCn.add(domain.getDnCn());
                } else {
                    dnCn.add(domain.getDnCn());
                    dnCn.add("www." + domain.getDnCn());
                }
                log.info("ダブルオプションあり");
            } else {
                dnCn.add(domain.getDnCn());
            }

            // wildcardなら2件のループ
            for (String cn : dnCn) {

                // SNI無効
                boolean disableSNI = true;

                // onemore flug
                boolean onemore = false;

                // 排他的論理和で判定
                while (disableSNI ^ onemore) {
                    try {
                        // 対象ドメインのHTTPS接続
                        if (PROFILE.equals("development")) {
                            // プロキシの名前解決ができないのでIPで指定
                            SocketAddress addr = new InetSocketAddress("172.18.6.18", 8080);
                            Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
                            connection = (HttpsURLConnection) (new URL("https://" + cn)).openConnection(proxy);
                        } else {
                            connection = (HttpsURLConnection) (new URL("https://" + cn)).openConnection();
                        }
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
                        log.info("SNIモード: " + !disableSNI);
                        log.info("検査対象情報: "
                                + cn + ", "
                                + readMapper.selectAgentName(domain.getJointAgentId()));

                        // connectionのタイムアウト設定
                        connection.setConnectTimeout(timeout);
                        connection.setReadTimeout(timeout);

                        // 設定をconnectionに反映
                        connection.setSSLSocketFactory(socketFactory);

                        // connectionの確立
                        connection.connect();

                        // 対象CNの証明書情報取得
                        Certificate[] certs = connection.getServerCertificates();

                        // Statusの更新
                        if (certs[0] instanceof X509Certificate) {
                            try {
                                // 証明書が有効か判定
                                ((X509Certificate) certs[0]).checkValidity();

                                String issuer = ((X509Certificate) certs[0]).getIssuerX500Principal().getName();

                                // statusのみ抽出
                                int startStatus = issuer.indexOf("CN=") + 3;

                                if (issuer.contains(",")) {
                                    int endStatus = issuer.indexOf(",");
                                    status = issuer.substring(startStatus, endStatus);
                                } else {
                                    status = issuer.substring(startStatus);
                                }
                                log.info("証明書は有効です: " + cn + ", STATUS: " + status + ", SNI: " + !disableSNI);
                            } catch (CertificateExpiredException e) {
                                log.info("証明書は失効しています: " + cn + ", SNI:" + !disableSNI);
                                status = "ERROR: " + e.getMessage();
                            }
                        } else {
                            log.info("証明書の型が不明です: " + cn + ", SNI: " + !disableSNI);
                            status = "ERROR: UNKNOWN CERTIFICATE TYPE";
                        }
                    } catch (Exception e) {
                        log.info("接続エラー: " + e.toString() + ": " + cn + ", SNI: " + !disableSNI);
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
            log.info("登録情報: " + domain.toString());

            // DB更新
            dao.updateDomainTable(domain);

            // 遅延1000msec
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                log.error(e.toString());
                e.getStackTrace();
            }
        }

        log.info("GetCert 正常終了");
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
