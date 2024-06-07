package com.fkp.tools.httpclient5.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
public class RestTemplateConfig {

    @Bean
    public HttpClient clientHttpRequestFactory() {
        final PoolingHttpClientConnectionManager connManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultSocketConfig(SocketConfig.custom()
                        //开启TCP nagle算法
                        .setTcpNoDelay(true)
                        .build())
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        //确定新连接完全建立之前的超时时间。
                        .setConnectTimeout(Timeout.ofMilliseconds(3000))
                        .setSocketTimeout(Timeout.ofMilliseconds(5000))
                        .setTimeToLive(TimeValue.ofHours(1))
                        .build())
                //设置连接池的总最大连接数
                .setMaxConnTotal(2000)
                //设置每个路由的最大连接数,ip+port为一个路由
                .setMaxConnPerRoute(2000)
                //支持tls并信任所有证书
                .setSSLSocketFactory(getTrustAllSslSocketFactory())
                .build();

        final RequestConfig defaultRequestConfig = RequestConfig.custom()
                //请求超时时间
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(30000))
                .setResponseTimeout(Timeout.ofMilliseconds(10000))
                .setExpectContinueEnabled(true)
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
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy())
                .build();
    }

    private SSLConnectionSocketFactory getTrustAllSslSocketFactory(){
        SSLConnectionSocketFactory socketFactory;
        SSLContext sslContext;
        //两种获取SSLConnectionSocketFactory方式
//        try {
//            sslContext = SSLContext.getInstance("TLS");
//            sslContext.init(null, new TrustManager[]{getTrustAllManager()}, null);
//            socketFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
//        }catch (Exception e){
//            throw new BeanCreationException("Build HttpClient Bean error, get ssl context exception.", e);
//        }

        //两种获取SSLConnectionSocketFactory方式
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
            socketFactory = SSLConnectionSocketFactoryBuilder.create().setSslContext(sslContext).setHostnameVerifier(new NoopHostnameVerifier()).build();
        }catch (Exception e){
            throw new BeanCreationException("Build HttpClient Bean error, get ssl context exception.", e);
        }
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
