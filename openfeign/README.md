## 引用方式
```xml
    <dependency>
        <groupId>com.fkp.tools</groupId>
        <artifactId>openfeign</artifactId>
        <version>1.0.0</version>
    </dependency>
```

## 配置示例
开启httpclient5支持，配置ssl示例，其他配置使用openfeign和feign-hc原生配置
```yaml
feign:
  httpclient:
    hc5:
      # 开启httpclient5支持
      enabled: true
  ssl:
    # 是否开启国密协议
    enable-gm-protocol: false
    # 是否关闭验证服务器证书
    disable-ssl-validation: false
    # provider选项，配置为swxa即可，需要有SwxaJce和SwxaJSSE依赖，在没有时可以使用sun，但是无法使用国密协议
    provider-name: swxa
    # 配置用户证书的类型，支持PKCS12和JKS，若是国密协议，则证书必须包含用途项且为双证
    key-store-type: pkcs12
    # 配置用户证书，类型需要和key-store-type指定的一致
    key-store: D:\IDEAWorkSpace\springboot-web-template\config\certs\RSA\pt_rsa_sign_client.pfx
    # 对应的证书密码
    key-store-pwd: swxa@2024
    # 配置服务器根证书，支持JKS和PEM证书格式，trust-key-store指定JKS证书trust-certificate指定PEM证书，二者选其一，同时配置优先trust-certificate
    trust-key-store: D:\IDEAWorkSpace\springboot-web-template\config\certs\RSA\root-ca.jks
    # 配置服务器根证书JKS的密码
    trust-key-store-pwd: swxa@2024
    # 配置服务器根证书PEM格式，trust-key-store指定JKS证书trust-certificate指定PEM证书，二者选其一，同时配置优先trust-certificate
#    trust-certificate: D:\IDEAWorkSpace\springboot-web-template\config\certs\RSA\pt_rsa_cer.cer
```
## 工具类
与openfeign底层使用的HttpClient5单例对象的工具类已自动创建到IOC容器，直接使用即可，示例

```java
@SpringBootTest
public class HttpClientTest {

    @Autowired
    private HttpClient5Utils httpClient5Utils;

    @BeforeAll
    static void init(){
        Security.addProvider(new SwxaProvider());
        Security.addProvider(new SwxaJsseProvider());
    }

    @Test
    void test(){
        Map<String, Object> body = new HashMap<>();
        body.put("keyName", "dsadas");
        Map<String, Object> headers = new HashMap<>();
        headers.put("X-SW-Authorization-Token", "eyJhbGciOiJTTTJXaXRoU00zIn0.eyJzdWIiOiJmMTA2MTM5MC0xMmVhLTdlMDUtYjU2OC1jNDkyNzcyYTNjODEiLCJhdWQiOiJmMTA2MTM5MC0xMmVhLTdlMDUtYjU2OC1jNDkyNzcyYTNjODEiLCJuYmYiOjE3MzIxNTU1MTEsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTAwMC9jY3NwL2F1dGgiLCJleHAiOjE3MzI3NjAzMTEsInR5cGUiOiJBS1NLIiwiaWF0IjoxNzMyMTU1NTExLCJzYW5zZWNfY3VzdG9tX3BhcmFtcyI6ImYxMDYxMzkwLTEyZWEtN2UwNS1iNTY4LWM0OTI3NzJhM2M4MSJ9.u3egvjBG0wjH93AC-ZrnZDSdSdPujegZ8EoDl6g_wCcFeXy_AiWhpjaHjFgnHmNMwa1Kdoi_TLAcQrdlmVTa9g");
        Map<String, Object> map = httpClient5Utils.postJson("https://10.0.120.104:20121/kms/v4/keys/get", body, headers);
        System.out.println(map);

    }
}
```

## 引用其他依赖
<p>io.github.openfeign:feign-hc5:11.10</p>
<p>org.springframework.cloud:spring-cloud-openfeign-core:3.19</p>
<p>org.apache.commons:commons-lang3:3.12.0</p>
<p>com.alibaba.fastjson2:fastjson2:2.0.51</p>
<p>org.springframework.boot:spring-boot-configuration-processor:2.7.18</p>
<p>org.springframework.boot:spring-boot-autoconfigure:2.7.18</p>
<p>org.slf4j:slf4j-api:1.7.36</p>
