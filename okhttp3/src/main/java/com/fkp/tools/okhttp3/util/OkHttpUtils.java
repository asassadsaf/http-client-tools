package com.fkp.tools.okhttp3.util;

import com.alibaba.fastjson2.JSON;
import com.fkp.tools.okhttp3.constant.MediaTypeValueConstant;
import com.fkp.tools.okhttp3.exception.HttpClientExecException;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author fkp
 * @version 1.0
 * @description
 * @date 2024/6/15 23:34
 */
@Component
public class OkHttpUtils {

    @Autowired
    private OkHttpClient okHttpClient;

    public Map<String, Object> get(String url) {
        return get(url, null, null);
    }

    public Map<String, Object> get(String url, Map<String, Object> params) {
        return get(url, params, null);
    }

    public Map<String, Object> get(String url, Map<String, Object> params, Map<String, Object> headers) {
        String urlWithParams = assemblyGetUrl(url, params);
        Headers.Builder headersBuilder = new Headers.Builder();

        try {
            for (Map.Entry<String, Object> entry : Optional.ofNullable(headers).orElse(Collections.emptyMap()).entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if(value instanceof String){
                    headersBuilder.add(key, (String) value);
                } else if(value instanceof Date){
                    headersBuilder.add(key, (Date) value);
                }else {
                    headersBuilder.add(key, String.valueOf(value));
                }
            }
        }catch (Exception e){
            throw new IllegalArgumentException("Headers value can must parse to string.", e);
        }

        try (Response response = okHttpClient.newCall(new Request.Builder().get().headers(headersBuilder.build())
                .url(urlWithParams).build()).execute()){
            ResponseBody body = response.body();
            String bodyStr = "";
            if(body != null){
                bodyStr = body.string();
            }
            return JSON.parseObject(bodyStr);
        }catch (Exception e){
            throw new HttpClientExecException(e);
        }
    }

    public Map<String, Object> postJson(String url){
        return postJson(url, null, null);
    }

    public Map<String, Object> postJson(String url, Map<String, Object> body){
        return postJson(url, body, null);
    }

    public Map<String, Object> postJson(String url, Map<String, Object> body, Map<String, Object> headers){
        RequestBody requestBody = RequestBody.create(JSON.toJSONBytes(Optional.ofNullable(body).orElse(Collections.emptyMap())),
                MediaType.parse(MediaTypeValueConstant.APPLICATION_JSON_VALUE));
        Headers.Builder headersBuilder = new Headers.Builder();
        try {
            for (Map.Entry<String, Object> entry : Optional.ofNullable(headers).orElse(Collections.emptyMap()).entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if(value instanceof String){
                    headersBuilder.add(key, (String) value);
                } else if(value instanceof Date){
                    headersBuilder.add(key, (Date) value);
                }else {
                    headersBuilder.add(key, String.valueOf(value));
                }
            }
        }catch (Exception e){
            throw new IllegalArgumentException("Headers value can must parse to string.", e);
        }
        try (Response response = okHttpClient.newCall(new Request.Builder().post(requestBody).headers(headersBuilder.build())
                .url(url).build()).execute()){
            ResponseBody responseBody = response.body();
            String bodyStr = "";
            if(responseBody != null){
                bodyStr = responseBody.string();
            }
            return JSON.parseObject(bodyStr);
        }catch (Exception e){
            throw new HttpClientExecException(e);
        }
    }

    public Map<String, Object> postFormData(String url){
        return postFormData(url, null, null);
    }

    public Map<String, Object> postFormData(String url, Map<String, Object> body){
        return postFormData(url, body, null);
    }

    public Map<String, Object> postFormData(String url, Map<String, Object> body, Map<String, Object> headers){
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        Optional.ofNullable(body).ifPresent(map -> {
            map.forEach((key, value) -> {
                if (value instanceof File) {
                    File fileValue = (File) value;
                    bodyBuilder.addFormDataPart(key, fileValue.getName(),
                            RequestBody.create(fileValue, MediaType.parse(MediaTypeValueConstant.MULTIPART_FORM_DATA_VALUE)));
                } else if (value instanceof byte[]) {
                    byte[] byteValue = (byte[]) value;
                    bodyBuilder.addFormDataPart(key, "byteData",
                            RequestBody.create(byteValue, MediaType.parse(MediaTypeValueConstant.MULTIPART_FORM_DATA_VALUE)));
                } else if (value instanceof String) {
                    String stringValue = (String) value;
                    bodyBuilder.addFormDataPart(key, stringValue);
                } else if (value instanceof Integer) {
                    Integer integerValue = (Integer) value;
                    bodyBuilder.addFormDataPart(key, String.valueOf(integerValue));
                } else {
                    throw new IllegalArgumentException("value must be byte array or file or string or integer.");
                }
            });
        });
        MultipartBody requestBody = bodyBuilder.build();
        Headers.Builder headersBuilder = new Headers.Builder();
        try {
            for (Map.Entry<String, Object> entry : Optional.ofNullable(headers).orElse(Collections.emptyMap()).entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if(value instanceof String){
                    headersBuilder.add(key, (String) value);
                } else if(value instanceof Date){
                    headersBuilder.add(key, (Date) value);
                }else {
                    headersBuilder.add(key, String.valueOf(value));
                }
            }
        }catch (Exception e){
            throw new IllegalArgumentException("Headers value can must parse to string.", e);
        }
        try (Response response = okHttpClient.newCall(new Request.Builder().post(requestBody).headers(headersBuilder.build())
                .url(url).build()).execute()){
            ResponseBody responseBody = response.body();
            String bodyStr = "";
            if(responseBody != null){
                bodyStr = responseBody.string();
            }
            return JSON.parseObject(bodyStr);
        }catch (Exception e){
            throw new HttpClientExecException(e);
        }
    }

    public Map<String, Object> postUrlEncoded(String url){
        return postUrlEncoded(url, null, null);
    }

    public Map<String, Object> postUrlEncoded(String url, Map<String, Object> body){
        return postUrlEncoded(url, body, null);
    }

    public Map<String, Object> postUrlEncoded(String url, Map<String, Object> body, Map<String, Object> headers){
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        try {
            for (Map.Entry<String, Object> entry : Optional.ofNullable(body).orElse(Collections.emptyMap()).entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if(value instanceof String){
                    bodyBuilder.add(key, (String) value);
                }else {
                    bodyBuilder.add(key, String.valueOf(value));
                }
            }
        }catch (Exception e){
            throw new IllegalArgumentException("Body value can must parse to string.", e);
        }
        Headers.Builder headersBuilder = new Headers.Builder();
        try {
            for (Map.Entry<String, Object> entry : Optional.ofNullable(headers).orElse(Collections.emptyMap()).entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if(value instanceof String){
                    headersBuilder.add(key, (String) value);
                } else if(value instanceof Date){
                    headersBuilder.add(key, (Date) value);
                }else {
                    headersBuilder.add(key, String.valueOf(value));
                }
            }
        }catch (Exception e){
            throw new IllegalArgumentException("Headers value can must parse to string.", e);
        }
        try (Response response = okHttpClient.newCall(new Request.Builder().post(bodyBuilder.build()).headers(headersBuilder.build()).url(url).build())
                     .execute()){
            ResponseBody responseBody = response.body();
            String bodyStr = "";
            if(responseBody != null){
                bodyStr = responseBody.string();
            }
            return JSON.parseObject(bodyStr);
        } catch (IOException e) {
            throw new HttpClientExecException(e);
        }
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
