package com.fkp.tools.openfeign.util;

import com.fkp.tools.openfeign.properties.CustomFeignHttpClientProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/**
 * @author fengkunpeng
 * @version 1.0
 * @description 读取证书文件工具类
 * @date 2024/10/25 15:05
 */
@Slf4j
public class CertUtils {
    private CertUtils(){}

    // type支持PKCS12,JKS
    public static KeyStore buildKeyStore(CustomFeignHttpClientProperties.KeyStoreTypeEnum typeEnum, String path,
                                         String pwd, CustomFeignHttpClientProperties.ProviderNameEnum providerNameEnum) throws Exception {
        try (InputStream inputStream = Files.newInputStream(Paths.get(path), StandardOpenOption.READ)){
            KeyStore keyStore = KeyStore.getInstance(typeEnum.name(), providerNameEnum.getKeyStoreName());
            keyStore.load(inputStream, pwd.toCharArray());
            log.debug("buildKeyStore type: {}, path: {}, providerName: {}", typeEnum, path, providerNameEnum);
            return keyStore;
        }
    }

    // 支持X.509的pem格式证书
    public static KeyStore buildTrustKeyStoreByPem(String path, CustomFeignHttpClientProperties.ProviderNameEnum providerNameEnum) throws Exception{
        KeyStore keyStore = KeyStore.getInstance(CustomFeignHttpClientProperties.KeyStoreTypeEnum.JKS.name(), providerNameEnum.getKeyStoreName());
        keyStore.load(null, null);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", providerNameEnum.getJceName());
        Certificate certificate;
        try (InputStream inputStream = Files.newInputStream(Paths.get(path), StandardOpenOption.READ)){
            certificate = certificateFactory.generateCertificate(inputStream);
        }
        keyStore.setCertificateEntry("userCert", certificate);
        log.debug("buildKeyStoreByPem type: {}, path: {}, providerName: {}", "pem", path, providerNameEnum);
        return keyStore;
    }
}
