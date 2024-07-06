package com.fkp.tools.httpclient5.util;

import com.alibaba.fastjson2.JSON;
import com.fkp.tools.httpclient5.exception.HttpClientExecException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author fengkunpeng
 * @version 1.0
 * @description httpclient5 utils - get/post-json/post-formdata/post-urlencoded
 * @date 2024/6/5 11:30
 */

@Slf4j
public class HttpClient5Utils {

    private final HttpClient httpClient;

    public HttpClient5Utils(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Map<String, Object> get(String url) {
        return get(url, null, null);
    }

    public Map<String, Object> get(String url, Map<String, Object> params) {
        return get(url, params, null);
    }

    public Map<String, Object> get(String url, Map<String, Object> params, Map<String, Object> headers) {
        HttpGet httpGet = new HttpGet(assemblyGetUrl(url, params));
        for (Map.Entry<String, Object> entry : Optional.ofNullable(headers).orElse(Collections.emptyMap()).entrySet()) {
            httpGet.addHeader(entry.getKey(), entry.getValue());
        }
        try {
            String res = httpClient.execute(httpGet, new BasicHttpClientResponseHandler());
            return JSON.parseObject(res);
        } catch (Exception e){
            throw new HttpClientExecException(e);
        }
    }

    public Map<String, Object> postJson(String url){
        return postJson(url, null, null, ContentType.APPLICATION_JSON);
    }

    public Map<String, Object> postJson(String url, Map<String, Object> body){
        return postJson(url, body, null, ContentType.APPLICATION_JSON);
    }

    public Map<String, Object> postJson(String url, Map<String, Object> body, Map<String, Object> headers){
        return postJson(url, body, headers, ContentType.APPLICATION_JSON);
    }

    public Map<String, Object> postFormData(String url){
        return postJson(url, null, null, ContentType.MULTIPART_FORM_DATA);
    }

    public Map<String, Object> postFormData(String url, Map<String, Object> body){
        return postJson(url, body, null, ContentType.MULTIPART_FORM_DATA);
    }

    public Map<String, Object> postFormData(String url, Map<String, Object> body, Map<String, Object> headers){
        return postJson(url, body, headers, ContentType.MULTIPART_FORM_DATA);
    }

    public Map<String, Object> postUrlEncoded(String url){
        return postJson(url, null, null, ContentType.APPLICATION_FORM_URLENCODED);
    }

    public Map<String, Object> postUrlEncoded(String url, Map<String, Object> body){
        return postJson(url, body, null, ContentType.APPLICATION_FORM_URLENCODED);
    }

    public Map<String, Object> postUrlEncoded(String url, Map<String, Object> body, Map<String, Object> headers){
        return postJson(url, body, headers, ContentType.APPLICATION_FORM_URLENCODED);
    }

    public Map<String, Object> postJson(String url, Map<String, Object> body, Map<String, Object> headers, ContentType contentType){
        HttpPost httpPost = new HttpPost(url);
        for (Map.Entry<String, Object> entry : Optional.ofNullable(headers).orElse(Collections.emptyMap()).entrySet()) {
            httpPost.addHeader(entry.getKey(), entry.getValue());
        }
        if (contentType.equals(ContentType.APPLICATION_JSON)) {
            httpPost.setEntity(buildJsonBody(body));
        } else if (contentType.equals(ContentType.APPLICATION_FORM_URLENCODED)) {
            httpPost.setEntity(buildUrlEncodedBody(body));
        } else if (contentType.equals(ContentType.MULTIPART_FORM_DATA)) {
            httpPost.setEntity(buildFormDataBody(body));
        }
        try {
            String res = httpClient.execute(httpPost, new BasicHttpClientResponseHandler());
            return JSON.parseObject(res);
        } catch (Exception e){
            throw new HttpClientExecException(e);
        }
    }

    private StringEntity buildJsonBody(Map<String, Object> params){
        return new StringEntity(JSON.toJSONString(Optional.ofNullable(params).orElse(Collections.emptyMap())),
                ContentType.APPLICATION_JSON, StandardCharsets.UTF_8.name(), false);
    }

    private HttpEntity buildFormDataBody(Map<String, Object> params){
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        for (Map.Entry<String, Object> entry : Optional.ofNullable(params).orElse(Collections.emptyMap()).entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if(value instanceof byte[]){
                builder.addBinaryBody(key, (byte[]) value);
            }else if(value instanceof File){
                builder.addBinaryBody(key, (File) value);
            }else if(value instanceof String){
                builder.addTextBody(key, (String) value);
            }else if(value instanceof Integer){
                builder.addTextBody(key, String.valueOf(value));
            }else {
                throw new IllegalArgumentException("value must be byte array or file or string or integer.");
            }
        }
        builder.setContentType(ContentType.MULTIPART_FORM_DATA);
        builder.setCharset(StandardCharsets.UTF_8);
        return builder.build();
    }

    private UrlEncodedFormEntity buildUrlEncodedBody(Map<String, Object> params){
        List<NameValuePair> nameValuePairList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : Optional.ofNullable(params).orElse(Collections.emptyMap()).entrySet()) {
            Object value = entry.getValue();
            if(value instanceof String){
                nameValuePairList.add(new BasicNameValuePair(entry.getKey(), (String) value));
            }else if(value instanceof Integer){
                nameValuePairList.add(new BasicNameValuePair(entry.getKey(), String.valueOf(value)));
            }else {
                throw new IllegalArgumentException("value must be string or integer.");
            }
        }
        return new UrlEncodedFormEntity(nameValuePairList, StandardCharsets.UTF_8);
    }

    private String assemblyGetUrl(String url, Map<String, Object> paramMap) {
        StringBuilder sb = new StringBuilder(url);
        boolean first = true;
        for (Map.Entry<String, Object> entry : Optional.ofNullable(paramMap).orElse(Collections.emptyMap()).entrySet()) {
            if(first) {
                sb.append("?");
            } else {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        return sb.toString();
    }
}
