package com.fkp.tools.okhttp3.config;

import okhttp3.*;
import okhttp3.internal.connection.RealConnectionPool;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * @author fkp
 * @version 1.0
 * @description
 * @date 2024/6/15 22:29
 */
@Configuration(proxyBeanMethods = false)
public class OkHttpConfig {

    @Bean
    public OkHttpClient okHttpClient(){
        X509TrustManager trustAllManager = getTrustAllManager();
        return new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(2000, 1, TimeUnit.HOURS))
                //配置tls，接受所有服务端证书
                .sslSocketFactory(getTrustAllSslSocketFactory(trustAllManager), trustAllManager)
                .hostnameVerifier(((hostname, session) -> true))
                //是否开启失败重试
                .retryOnConnectionFailure(false)
                //socket连接超时时间
                .connectTimeout(30, TimeUnit.SECONDS)
                //从socket读取数据超时时间
                .readTimeout(30, TimeUnit.SECONDS)
                //向socket写入数据超时时间
                .writeTimeout(30, TimeUnit.SECONDS)
                //同步/异步调用返回响应的超时时间
                .callTimeout(30, TimeUnit.SECONDS)
                //定时向服务段发送消息保持长连接，在http2和websocket有效
//                .pingInterval(1, TimeUnit.SECONDS)
                //添加拦截器
//                .addInterceptor(new MyOkHttpInterceptor())
                .build();
    }

    //自定义OkHttp的拦截器，设置请求头
    private static class MyOkHttpInterceptor implements Interceptor{
        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Request request = chain.request().newBuilder().addHeader("token", "abc").build();
            return chain.proceed(request);
        }
    }


    private SSLSocketFactory getTrustAllSslSocketFactory(X509TrustManager trustManager){
        SSLSocketFactory socketFactory;
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new X509TrustManager[]{trustManager}, null);
            socketFactory = sslContext.getSocketFactory();
        }catch (Exception e){
            throw new BeanCreationException("Build HttpClient Bean error, get ssl context exception.", e);
        }
        return socketFactory;
    }

    private X509TrustManager getTrustAllManager(){
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }
}
