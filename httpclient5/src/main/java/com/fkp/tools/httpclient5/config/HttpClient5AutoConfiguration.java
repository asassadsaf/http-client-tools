package com.fkp.tools.httpclient5.config;

import com.fkp.tools.httpclient5.prop.HttpClientConfigProperties;
import com.fkp.tools.httpclient5.util.HttpClient5Utils;
import com.fkp.tools.httpclient5.util.SocketUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
//import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author fengkunpeng
 * @version 1.0
 * @description HttpClient5配置类
 * @date 2024/6/6 10:59
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
@ConditionalOnClass(HttpClient.class)
@ConditionalOnProperty(prefix = "tools.http-client5", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(HttpClientConfigProperties.class)
public class HttpClient5AutoConfiguration {

    @Bean
    public HttpClient5Utils httpClientUtils(HttpClient httpClient){
        return new HttpClient5Utils(httpClient);
    }

    @Bean
    @ConditionalOnMissingBean(HttpClient.class)
    public HttpClient httpClient(HttpClientConfigProperties properties) {
        final PoolingHttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultSocketConfig(SocketConfig.custom()
                        //开启TCP nagle算法
                        .setTcpNoDelay(properties.getTcpNoDelay())
                        .setSoTimeout(Timeout.ofMilliseconds(properties.getSocketTimeout()))
                        .build())
//                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        //确定新连接完全建立之前的超时时间。
//                        .setConnectTimeout(Timeout.ofMilliseconds(properties.getConnectTimeout()))
//                        .setSocketTimeout(Timeout.ofMilliseconds(properties.getSocketTimeout()))
//                        .setTimeToLive(TimeValue.ofMilliseconds(properties.getTimeToLive()))
//                        .build())
                .setConnectionTimeToLive(TimeValue.ofMilliseconds(properties.getTimeToLive()))
                //设置连接池的总最大连接数
                .setMaxConnTotal(properties.getMaxConnTotal())
                //设置每个路由的最大连接数,ip+port为一个路由
                .setMaxConnPerRoute(properties.getMaxConnPerRoute())
                //支持tls并信任所有证书
                .setSSLSocketFactory(properties.getSupportSsl() ? getTrustAllSslSocketFactory(properties.getTlcpProtocol()) : null)
                .build();

        final RequestConfig defaultRequestConfig = RequestConfig.custom()
                //请求超时时间
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(properties.getConnectionRequestTimeout()))
                .setResponseTimeout(Timeout.ofMilliseconds(properties.getResponseTimeout()))
                .setExpectContinueEnabled(properties.getExpectContinueEnabled())
                .build();

        return HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(defaultRequestConfig)
                //实现短链接，设置保活策略为自定义实现类且为空，若不设置默认长连接
//                .setKeepAliveStrategy(new ConnectionKeepAliveStrategy(){
//
//                    @Override
//                    public TimeValue getKeepAliveDuration(HttpResponse response, HttpContext context) {
//                        return null;
//                    }
//                })
                //不设置或设置为null默认为DefaultConnectionKeepAliveStrategy.INSTANCE长连接
                .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
                //与setKeepAliveStrategy相同，不设置或设置为null默认为DefaultHttpRequestRetryStrategy.INSTANCE
                .setRetryStrategy(DefaultHttpRequestRetryStrategy.INSTANCE)
                .build();
    }

    private SSLConnectionSocketFactory getTrustAllSslSocketFactory(Boolean tlcpProtocol) {
        SSLConnectionSocketFactory socketFactory;
        SSLContext sslContext;
        //两种获取SSLConnectionSocketFactory方式
        if (tlcpProtocol) {
            try {
                sslContext = SocketUtils.genSslContext(null, SocketUtils.genNoValidateTrustManager(), new SecureRandom(), true);
                socketFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
            } catch (Exception e) {
                throw new BeanCreationException("Build HttpClient Bean error, get ssl context exception.", e);
            }
        } else {
            try {
                sslContext = SSLContexts.custom().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
                socketFactory = SSLConnectionSocketFactoryBuilder.create().setSslContext(sslContext).setHostnameVerifier(new NoopHostnameVerifier()).build();
            } catch (Exception e) {
                throw new BeanCreationException("Build HttpClient Bean error, get ssl context exception.", e);
            }
        }
        log.debug("SSLContext provider name: {}", sslContext.getProvider().getName());
//        try {
//            sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, new TrustManager[]{getTrustAllManager()}, null);
//            socketFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
//        }catch (Exception e){
//            throw new BeanCreationException("Build HttpClient Bean error, get ssl context exception.", e);
//        }

        //两种获取SSLConnectionSocketFactory方式
        return socketFactory;
    }

    private TrustManager getTrustAllManager(){
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
    }
}
