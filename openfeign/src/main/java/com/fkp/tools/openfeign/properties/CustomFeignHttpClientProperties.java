package com.fkp.tools.openfeign.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author fengkunpeng
 * @version 1.0
 * @description
 * @date 2024/11/22 9:33
 */
@ConfigurationProperties(prefix = "feign.ssl")
@Data
public class CustomFeignHttpClientProperties {
    private boolean enableGmProtocol = false;
    private boolean disableSslValidation = false;
    private KeyStoreTypeEnum keyStoreType = KeyStoreTypeEnum.JKS;
    private String keyStore;
    private String keyStorePwd;
    private String trustKeyStore;
    private String trustKeyStorePwd;
    private String trustCertificate;
    private ProviderNameEnum providerName = ProviderNameEnum.SUN;

    @Getter
    public enum ProviderNameEnum{
        SUN("SUN", "SunJCE", "SunJSSE", "SunX509", "SunX509"),
        SWXA("SwxaJCE", "SwxaJCE", "SwxaJSSE", "SwxaX509", "PKIX")
        ;

        private final String keyStoreName;
        private final String jceName;
        private final String jsseName;
        private final String keyManagerFactoryAlg;
        private final String trustManagerFactoryAlg;

        ProviderNameEnum(String keyStoreName, String jceName, String jsseName, String keyManagerFactoryAlg, String trustManagerFactoryAlg) {
            this.keyStoreName = keyStoreName;
            this.jceName = jceName;
            this.jsseName = jsseName;
            this.keyManagerFactoryAlg = keyManagerFactoryAlg;
            this.trustManagerFactoryAlg = trustManagerFactoryAlg;
        }

        public String getKeyStoreName(KeyStoreTypeEnum keyStoreTypeEnum){
            if(this.equals(SUN) && KeyStoreTypeEnum.PKCS12.equals(keyStoreTypeEnum)){
                return this.jsseName;
            }
            return this.keyStoreName;

        }
    }

    public enum KeyStoreTypeEnum{
        JKS,
        PKCS12
    }
}
