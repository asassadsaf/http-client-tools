package com.fkp.tools.openfeign.config;

import com.fkp.tools.openfeign.exception.ConfigurationItemInvalidException;
import com.fkp.tools.openfeign.exception.SSLConfigurationException;
import com.fkp.tools.openfeign.properties.CustomFeignHttpClientProperties;
import com.fkp.tools.openfeign.util.CertUtils;
import com.fkp.tools.openfeign.util.HttpClient5Utils;
import com.fkp.tools.openfeign.util.SocketUtils;
import feign.hc5.ApacheHttp5Client;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.LayeredConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * @author fengkunpeng
 * @version 1.0
 * @description
 * @date 2024/11/21 22:14
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "feign.httpclient.hc5.enabled", havingValue = "true")
@ConditionalOnClass({ApacheHttp5Client.class, CloseableHttpClient.class})
@EnableConfigurationProperties({FeignHttpClientProperties.class, CustomFeignHttpClientProperties.class})
public class CustomHttpClient5FeignAutoConfiguration {

    // 自定义org.apache.hc.client5.http.io.HttpClientConnectionManager以支持配置tls、tlcp
    // 代替org.springframework.cloud.openfeign.clientconfig.HttpClient5FeignConfiguration#hc5ConnectionManager
    @Bean
    public HttpClientConnectionManager hc5ConnectionManager(FeignHttpClientProperties httpClientProperties, LayeredConnectionSocketFactory layeredConnectionSocketFactory) {
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(layeredConnectionSocketFactory)
                .setMaxConnTotal(httpClientProperties.getMaxConnections())
                .setMaxConnPerRoute(httpClientProperties.getMaxConnectionsPerRoute())
                .setConnPoolPolicy(PoolReusePolicy.valueOf(httpClientProperties.getHc5().getPoolReusePolicy().name()))
                .setPoolConcurrencyPolicy(
                        PoolConcurrencyPolicy.valueOf(httpClientProperties.getHc5().getPoolConcurrencyPolicy().name()))
                .setConnectionTimeToLive(
                        TimeValue.of(httpClientProperties.getTimeToLive(), httpClientProperties.getTimeToLiveUnit()))
                .setDefaultSocketConfig(
                        SocketConfig.custom().setSoTimeout(Timeout.of(httpClientProperties.getHc5().getSocketTimeout(),
                                httpClientProperties.getHc5().getSocketTimeoutUnit())).build())
                .build();
    }

    // 抑制方法名未使用驼峰命名警告
    @SuppressWarnings("all")
    @Bean
    public LayeredConnectionSocketFactory httpsSSLConnectionSocketFactory(CustomFeignHttpClientProperties properties) {
        // 添加TLSv1.1版本，TLCPv1.1使用的是TLSv1.1
        // 添加jdk版本判断，jdk1.8 [TLSv1, TLSv1.1, TLSv1.2]  jdk17:[TLSv1.2, TLSv1.3]
        final SSLConnectionSocketFactoryBuilder sslConnectionSocketFactoryBuilder = SSLConnectionSocketFactoryBuilder.create()
                .setTlsVersions(getTlsVersionsByJdkVersion());
        // 添加国密tls协议: TLCPv1.1
        if(properties.isEnableGmProtocol()){
            sslConnectionSocketFactoryBuilder.setTlsVersions("TLCPv1.1");
        }

        SSLContext sslContext;
        try {
            KeyManager[] keyManagers = null;
            TrustManager[] trustManagers = SocketUtils.genNoValidateTrustManager();
            if(StringUtils.isNotBlank(properties.getKeyStore())){
                // 配置客户端公私钥证书
                if(StringUtils.isBlank(properties.getKeyStorePwd())){
                    throw new ConfigurationItemInvalidException("key store password is not be blank.");
                }
                KeyStore keyStore = CertUtils.buildKeyStore(properties.getKeyStoreType(), properties.getKeyStore(), properties.getKeyStorePwd(), properties.getProviderName());
                keyManagers = SocketUtils.genKeyManagerFactory(keyStore, properties.getKeyStorePwd(), properties.isEnableGmProtocol());
            }
            KeyStore trustKeyStore = null;
            if(StringUtils.isNotBlank(properties.getTrustKeyStore())){
                // 配置服务器根证JKS证书格式
                if(StringUtils.isBlank(properties.getTrustKeyStorePwd())){
                    throw new ConfigurationItemInvalidException("trust key store password is not be blank.");
                }
                trustKeyStore = CertUtils.buildKeyStore(CustomFeignHttpClientProperties.KeyStoreTypeEnum.JKS, properties.getTrustKeyStore(), properties.getTrustKeyStorePwd(), properties.getProviderName());
            }else if(StringUtils.isNotBlank(properties.getTrustCertificate())){
                // 配置服务器根证pem证书格式
                trustKeyStore = CertUtils.buildTrustKeyStoreByPem(properties.getTrustCertificate(), properties.getProviderName());
            }
            if(trustKeyStore != null && !properties.isDisableSslValidation()){
                trustManagers = SocketUtils.genTrustManagerFactory(trustKeyStore, properties.isEnableGmProtocol());
            }
            sslContext = SocketUtils.genSslContext(keyManagers, trustManagers, new SecureRandom(), properties.isEnableGmProtocol());
        }catch (Exception e){
            throw new SSLConfigurationException("configure ssl error.", e);
        }
        sslConnectionSocketFactoryBuilder.setSslContext(sslContext).setHostnameVerifier(new NoopHostnameVerifier());
        return sslConnectionSocketFactoryBuilder.build();
    }

    @Bean
    public HttpClient5Utils httpClient5Utils(CloseableHttpClient httpClient){
        return new HttpClient5Utils(httpClient);
    }

    private TLS[] getTlsVersionsByJdkVersion(){
        String jdkVersion = StringUtils.defaultIfBlank(System.getProperty("java.version"), "1.8.0");
        if (jdkVersion.startsWith("1.7") || jdkVersion.startsWith("1.8")) {
            return new TLS[]{ TLS.V_1_1, TLS.V_1_2};
        }else if(jdkVersion.startsWith("11.0") || jdkVersion.startsWith("13.0") || jdkVersion.startsWith("17.0")){
            return new TLS[]{TLS.V_1_1, TLS.V_1_2, TLS.V_1_3};
        }
        return new TLS[]{TLS.V_1_1, TLS.V_1_2};
    }

}
