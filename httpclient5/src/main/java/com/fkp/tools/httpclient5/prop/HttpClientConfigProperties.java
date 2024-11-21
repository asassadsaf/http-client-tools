package com.fkp.tools.httpclient5.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author fengkunpeng
 * @version 1.0
 * @description
 * @date 2024/6/30 15:13
 */
@ConfigurationProperties(prefix = "tools.http-client5")
@Data
public class HttpClientConfigProperties {
    private Boolean tcpNoDelay = true;
    private Long connectTimeout = 30000L;
    private Long socketTimeout = 30000L;
    private Long timeToLive = 3600000L;
    private Integer maxConnTotal = 2000;
    private Integer maxConnPerRoute = 2000;
    private Boolean supportSsl = true;
    private Boolean tlcpProtocol = false;
    private Long connectionRequestTimeout = 30000L;
    private Long responseTimeout = 10000L;
    private Boolean expectContinueEnabled = true;
    private Boolean enabled = true;
}
