package com.fkp.tools.httpclient5;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.StandardAuthScheme;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.auth.CredentialsProviderBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.ManagedHttpClientConnectionFactory;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import org.apache.hc.core5.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.hc.core5.http.impl.io.DefaultHttpResponseParser;
import org.apache.hc.core5.http.impl.io.DefaultHttpResponseParserFactory;
import org.apache.hc.core5.http.io.*;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicLineParser;
import org.apache.hc.core5.http.message.LineParser;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.CharArrayBuffer;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

public class HttpClientConfig {


    public  CloseableHttpClient httpClient() {
        // Use custom message parser / writer to customize the way HTTP
        // messages are parsed from and written out to the data stream.
        // (使用自定义消息解析器/编写器来自定义从数据流解析HTTP消息和将其写入数据流的方式。)
        final HttpMessageParserFactory<ClassicHttpResponse> responseParserFactory = new DefaultHttpResponseParserFactory() {

            @Override
            public HttpMessageParser<ClassicHttpResponse> create(final Http1Config h1Config) {
                final LineParser lineParser = new BasicLineParser() {

                    @Override
                    public Header parseHeader(final CharArrayBuffer buffer) {
                        try {
                            return super.parseHeader(buffer);
                        } catch (final ParseException ex) {
                            return new BasicHeader(buffer.toString(), null);
                        }
                    }

                };
                return new DefaultHttpResponseParser(lineParser, DefaultClassicHttpResponseFactory.INSTANCE, h1Config);
            }

        };
        final HttpMessageWriterFactory<ClassicHttpRequest> requestWriterFactory = new DefaultHttpRequestWriterFactory();

        // Create HTTP/1.1 protocol configuration (创建HTTP1.1协议配置,基本用不到)
        final Http1Config h1Config = Http1Config.custom()
                .setMaxHeaderCount(200) //
                .setMaxLineLength(2000)
                .build();
        // Create connection configuration (创建连接配置)
        final CharCodingConfig connectionConfig = CharCodingConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE) //该方法用于设置字符编码遇到错误输入时的处理行为,IGNORE：忽略错误输入，继续进行字符编码。
                .setUnmappableInputAction(CodingErrorAction.IGNORE) //用于设置字符编码遇到无法映射的输入时的处理行为
                .setCharset(StandardCharsets.UTF_8) //设置字符编码。可以指定请求和响应的字符编码
                .build();

        // Use a custom connection factory to customize the process of
        // initialization of outgoing HTTP connections. Beside standard connection
        // configuration parameters HTTP connection factory can define message
        // parser / writer routines to be employed by individual connections.
        // (使用自定义连接工厂来自定义传出HTTP连接的初始化过程。除了标准连接配置参数之外，HTTP连接工厂还可以定义单个连接所使用的消息解析器/编写器例程。)
        // 通常情况下，不需要直接使用 HttpConnectionFactory 接口，而是通过 HttpClient 配置来创建 HTTP 连接
        final HttpConnectionFactory<ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(
                h1Config, connectionConfig, requestWriterFactory, responseParserFactory);
        //通过连接工厂创建连接
        // ManagedHttpClientConnection connection = connFactory.createConnection(Socket var1);

        // Client HTTP connection objects when fully initialized can be bound to
        // an arbitrary network socket. The process of network socket initialization,
        // its connection to a remote address and binding to a local one is controlled
        // by a connection socket factory.
        //(客户端HTTP连接对象在完全初始化时可以绑定到任意网络套接字。网络套接字初始化、连接到远程地址和绑定到本地地址的过程由连接套接字工厂控制。)

        // SSL context for secure connections can be created either based on
        // system or application specific properties.
        //(安全连接的SSL上下文可以基于系统或应用程序特定的属性创建。)
        final SSLContext sslContext = SSLContexts.createSystemDefault();

        // Create a registry of custom connection socket factories for supported
        // protocol schemes.
        //(为支持的协议方案创建自定义连接套接字工厂的注册表。)
        final Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslContext))
                .build();

        // Use custom DNS resolver to override the system DNS resolution.
        // 使用自定义 DNS 解析器覆盖系统 DNS 解析。
        final DnsResolver dnsResolver = new SystemDefaultDnsResolver() {

            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                if (host.equalsIgnoreCase("myhost")) {
                    return new InetAddress[] { InetAddress.getByAddress(new byte[] {127, 0, 0, 1}) };
                } else {
                    return super.resolve(host);
                }
            }

        };

        // Create a connection manager with custom configuration.
        // 使用自定义配置创建连接管理器。
        // PoolConcurrencyPolicy 连接池的并发性策略 STRICT：严格模式,在并发访问连接时，只有一个线程可以访问连接  LENIENT：宽松模式,在并发访问连接时，允许多个线程同时访问连接
        // PoolReusePolicy 连接池的连接重用策略:LIFO：后进先出，连接池返回连接时，优先选择最近返回的连接进行复用。 FIFO：先进先出，连接池返回连接时，优先选择最早返回的连接进行复用
        // TimeValue.ofMinutes(5) 时间值类型，用于设置连接在连接池中的最大空闲时间
        // dnsResolver DNS 解析器，用于解析主机名到 IP 地址
        final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry, PoolConcurrencyPolicy.STRICT, PoolReusePolicy.LIFO, TimeValue.ofMinutes(5),
                null, dnsResolver, null);

        // Configure the connection manager to use socket configuration either
        // by default or for a specific host.
        // (将连接管理器配置为在默认情况下或针对特定主机使用套接字配置。)
        connManager.setDefaultSocketConfig(SocketConfig.custom()
                .setTcpNoDelay(true)
                .build());
        // Validate connections after 10 sec of inactivity
        // 10 秒不活动后验证连接
        connManager.setDefaultConnectionConfig(ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(30)) //与服务端建立连接超时时间
                .setSocketTimeout(Timeout.ofSeconds(30)) // 设置套接字超时时间，即等待响应数据的超时时间。
                .setValidateAfterInactivity(TimeValue.ofSeconds(10)) // 连接验证间隔时间。当一个连接在池中空闲时间超过此值时，连接将被验证是否可用
                .setTimeToLive(TimeValue.ofHours(1)) //定义连接可以保持活动状态或执行请求的总时间跨度。
                .build());


        // Use TLS v1.3 only
        // 仅使用 TLS v1.3
        connManager.setDefaultTlsConfig(TlsConfig.custom() //用于配置 TLS 相关的参数
                .setHandshakeTimeout(Timeout.ofSeconds(30)) //设置 TLS 握手超时时间
                .setSupportedProtocols(TLS.V_1_3) //设置 TLS 握手超时时间
                .build());

        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        // 为可以保留在池中或由连接管理器租用的持久连接配置总的最大或每条路由限制。
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(10);
        connManager.setMaxPerRoute(new HttpRoute(new HttpHost("somehost", 80)), 20);


        // Use custom cookie store if necessary.
        // 如有必要，使用自定义 cookie 存储
        final CookieStore cookieStore = new BasicCookieStore();
        // Use custom credentials provider if necessary.
        // 如有必要，请使用自定义凭据提供程序。
        final CredentialsProvider credentialsProvider = CredentialsProviderBuilder.create()
                .build();
        // Create global request configuration
        // 创建全局请求配置
        final RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setCookieSpec(StandardCookieSpec.STRICT)
                .setExpectContinueEnabled(true)
                .setTargetPreferredAuthSchemes(Arrays.asList(StandardAuthScheme.NTLM, StandardAuthScheme.DIGEST))
                .setProxyPreferredAuthSchemes(Collections.singletonList(StandardAuthScheme.BASIC))
                .build();

        final CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager) //设置 HttpClient 的连接管理器
                .setDefaultCookieStore(cookieStore) //设置默认的 Cookie 存储
                .setDefaultCredentialsProvider(credentialsProvider) //设置默认的凭据提供器
                .setProxy(new HttpHost("myproxy", 8080)) //设置代理服务器
                .setDefaultRequestConfig(defaultRequestConfig) //设置默认的请求配置
                .build();

        return httpClient;

    }

}
