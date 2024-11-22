package com.fkp.tools.openfeign.util;

import com.fkp.tools.openfeign.properties.CustomFeignHttpClientProperties;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author fengkunpeng
 * @version 1.0
 * @description
 * @date 2024/10/27 14:04
 */

@Slf4j
public class SocketUtils {
    private SocketUtils(){}

    private static final String TLS_PROTOCOL = "TLS";
    private static final String TLCP_PROTOCOL = "TLCPv1.1";

    public static SSLContext genSslContext(KeyManager[] keyManagers, TrustManager[] trustManagers, SecureRandom secureRandom, boolean isGm) throws NoSuchAlgorithmException, KeyManagementException, NoSuchProviderException {
        SSLContext sslContext;
        if(isGm){
            sslContext = SSLContext.getInstance(TLCP_PROTOCOL, CustomFeignHttpClientProperties.ProviderNameEnum.SWXA.getJsseName());
            sslContext.init(keyManagers, trustManagers, secureRandom);
        }else {
            sslContext = SSLContext.getInstance(TLS_PROTOCOL, CustomFeignHttpClientProperties.ProviderNameEnum.SUN.getJsseName());
            sslContext.init(keyManagers, trustManagers, secureRandom);
        }
        log.debug("SSLContext isGm: {}, provider: {}, protocol: {}", isGm, sslContext.getProvider().getName(), sslContext.getProtocol());
        return sslContext;
    }

    public static KeyManager[] genKeyManagerFactory(KeyStore keyStore, String pwd, boolean isGm) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, NoSuchProviderException {
        KeyManagerFactory keyManagerFactory;
        if(isGm){
            keyManagerFactory = KeyManagerFactory.getInstance(CustomFeignHttpClientProperties.ProviderNameEnum.SWXA.getKeyManagerFactoryAlg(), CustomFeignHttpClientProperties.ProviderNameEnum.SWXA.getJsseName());
        }else {
            keyManagerFactory = KeyManagerFactory.getInstance(CustomFeignHttpClientProperties.ProviderNameEnum.SUN.getKeyManagerFactoryAlg(), CustomFeignHttpClientProperties.ProviderNameEnum.SUN.getJsseName());
        }
        keyManagerFactory.init(keyStore, pwd.toCharArray());
        log.debug("KeyManagerFactory isGm: {}, provider: {}, alg: {}", isGm, keyManagerFactory.getProvider().getName(), keyManagerFactory.getAlgorithm());
        return keyManagerFactory.getKeyManagers();
    }

    public static TrustManager[] genTrustManagerFactory(KeyStore keyStore, boolean isGm) throws NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException {
        TrustManagerFactory trustManagerFactory;
        if(isGm){
            trustManagerFactory = TrustManagerFactory.getInstance(CustomFeignHttpClientProperties.ProviderNameEnum.SWXA.getTrustManagerFactoryAlg(), CustomFeignHttpClientProperties.ProviderNameEnum.SWXA.getJsseName());
        }else {
            trustManagerFactory = TrustManagerFactory.getInstance(CustomFeignHttpClientProperties.ProviderNameEnum.SUN.getTrustManagerFactoryAlg(), CustomFeignHttpClientProperties.ProviderNameEnum.SUN.getJsseName());
        }
        trustManagerFactory.init(keyStore);
        log.debug("TrustManagerFactory isGm: {}, provider: {}, alg: {}", isGm, trustManagerFactory.getProvider().getName(), trustManagerFactory.getAlgorithm());
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
        log.debug("generate not validate all cert trust managers.");
        return new TrustManager[]{trustManager};
    }
}
