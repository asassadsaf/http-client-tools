package com.fkp.tools.httpclient5.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * @author fengkunpeng
 * @version 1.0
 * @description 读取证书文件工具类
 * @date 2024/10/25 15:05
 */
public class CertUtils {

    private static final String PROVIDE_NAME = "SwxaJCE";
    private static final String ALGORITHM = "X.509";      //SwxaX509 X509  PKI
//    private static final String PROVIDE_NAME = BouncyCastleProvider.PROVIDER_NAME;

    private CertUtils(){}

    public static KeyStore genKeyStoreByPkcs12(String filePath, String pwd) throws KeyStoreException, NoSuchProviderException, CertificateException, IOException, NoSuchAlgorithmException {
        try (InputStream in = Files.newInputStream(Paths.get(filePath))){
            KeyStore keyStore = KeyStore.getInstance("PKCS12", "SunJSSE");
            keyStore.load(in, pwd.toCharArray());
            System.out.println("PKCS12 KeyStore provider: " + keyStore.getProvider().getName());
            return keyStore;
        }
    }

    public static KeyStore genKeyStoreByJks(String content, String pwd) throws KeyStoreException, NoSuchProviderException, CertificateException, IOException, NoSuchAlgorithmException {
//        try (InputStream in = new ByteArrayInputStream(Base64.decodeBase64("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"))){
//            KeyStore keyStore = KeyStore.getInstance("JKS", PROVIDE_NAME);
//            keyStore.load(in, pwd.toCharArray());
//            System.out.println("JKS KeyStore provider: " + keyStore.getProvider().getName());
//            return keyStore;
//        }
        try (InputStream in = Files.newInputStream(Paths.get(content))){
            KeyStore keyStore = KeyStore.getInstance("JKS", "SwxaJCE");
            keyStore.load(in, pwd.toCharArray());
            System.out.println("JKS KeyStore provider: " + keyStore.getProvider().getName());
            return keyStore;
        }
    }

    public static KeyStore genKeyStoreByX509Pem(String filePath) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
        try (InputStream in = Files.newInputStream(Paths.get(filePath))){
            KeyStore keyStore = KeyStore.getInstance("JKS", PROVIDE_NAME);
            keyStore.load(null, null);
            CertificateFactory certificateFactory = CertificateFactory.getInstance(ALGORITHM, PROVIDE_NAME);
            Certificate certificate = certificateFactory.generateCertificate(in);
            keyStore.setCertificateEntry("root-ca", certificate);
            return keyStore;
        }
    }
}
