package com.fkp.tools.httpclient5.util;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author fengkunpeng
 * @version 1.0
 * @description
 * @date 2024/10/27 14:04
 */
public class SocketUtils {
    private SocketUtils(){}

    private static final String PROVIDER = "SwxaJSSE";
    private static final String ALGORITHM = "SwxaX509";      //SwxaX509 X509  PKI
    private static final String PROTOCOL_VERSION = "TLCPv1.1";      //"TLSv1.2", "TLSv1.1", "TLSv1", "TLCPv1.1"

    public static SSLSocketFactory  genSslSocketFactory(String keyStorePath, String pwd, String trustKeyStorePath, boolean isGm) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException, IOException, NoSuchProviderException {
//        KeyManager[] keyManagers = genKeyManagerFactory(CertUtils.genKeyStoreByJks(keyStorePath, pwd), pwd, isGm);
//        TrustManager[] trustManagers = genTrustManagerFactory(CertUtils.genKeyStoreByX509Pem(trustKeyStorePath), isGm);
        SecureRandom secureRandom = new SecureRandom();
        System.out.println("SecureRandom provider: " + secureRandom.getProvider().getName());
        SSLContext sslContext = genSslContext(null, genNoValidateTrustManager(), secureRandom, isGm);
//        SSLContext sslContext = genSslContext(keyManagers, trustManagers, new SecureRandom(), isGm);
        return sslContext.getSocketFactory();
    }

    public static SSLServerSocketFactory  genSslServerSocketFactory(String keyStorePath, String pwd, String trustKeyStorePath, boolean isGm) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, CertificateException, IOException, NoSuchProviderException {
        KeyStore keyStore;
        if (isGm) {
            keyStore = CertUtils.genKeyStoreByJks(keyStorePath, pwd);
        } else {
            keyStore = CertUtils.genKeyStoreByPkcs12(keyStorePath, pwd);
        }
        KeyManager[] keyManagers = genKeyManagerFactory(keyStore, pwd, isGm);
//        TrustManager[] trustManagers = genTrustManagerFactory(CertUtils.genKeyStoreByPkcs12(trustKeyStorePath, pwd), isGm);
//        TrustManager[] trustManagers = genNoValidateTrustManager();
        SecureRandom secureRandom = new SecureRandom();
        System.out.println("SecureRandom provider: " + secureRandom.getProvider().getName());
        SSLContext sslContext = genSslContext(keyManagers, null, secureRandom, isGm);
        return sslContext.getServerSocketFactory();
    }

    public static SSLContext genSslContext(KeyManager[] keyManagers, TrustManager[] trustManagers, SecureRandom secureRandom, boolean isGm) throws NoSuchAlgorithmException, KeyManagementException, NoSuchProviderException {
        SSLContext sslContext;
        if(isGm){
            sslContext = SSLContext.getInstance(PROTOCOL_VERSION, PROVIDER);
            sslContext.init(keyManagers, trustManagers, secureRandom);
        }else {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, secureRandom);
        }
        System.out.println("SSLContext provider: " + sslContext.getProvider().getName());
        return sslContext;
    }

    private static KeyManager[] genKeyManagerFactory(KeyStore keyStore, String pwd, boolean isGm) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, NoSuchProviderException {
        KeyManagerFactory keyManagerFactory;
        if(isGm){
            keyManagerFactory = KeyManagerFactory.getInstance(ALGORITHM, PROVIDER);
        }else {
            keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        }
        keyManagerFactory.init(keyStore, pwd.toCharArray());
        System.out.println("KeyManagerFactory provider: " + keyManagerFactory.getProvider().getName());
        return keyManagerFactory.getKeyManagers();
    }

    private static TrustManager[] genTrustManagerFactory(KeyStore keyStore, boolean isGm) throws NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException {
        TrustManagerFactory trustManagerFactory;
        if(isGm){
            trustManagerFactory = TrustManagerFactory.getInstance("PKIX", PROVIDER);
        }else {
            trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        }
        trustManagerFactory.init(keyStore);
        return trustManagerFactory.getTrustManagers();
    }

    /**
     * 返回一个不认证对方ssl证书的TrustManager数组，即无需传入对方ssl证书的根证书
     * @return 不认证根证书的TrustManager数组
     */
    public static TrustManager[] genNoValidateTrustManager(){
        // 使用hutool工具类中的DefaultTrustManager等同于以下代码
        TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}
            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        return new TrustManager[]{trustManager};
    }
}
