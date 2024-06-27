package com.fkp.tools.okhttp3;

import com.alibaba.fastjson2.JSONObject;
import com.fkp.tools.okhttp3.util.OkHttpUtils;
import lombok.SneakyThrows;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.Map;

@SpringBootTest
class Okhttp3ApplicationTests {

    @Autowired
    private OkHttpClient client;

    @Autowired
    private OkHttpUtils okHttpUtils;

    @SneakyThrows
    @Test
    void contextLoads() {
        Headers headers = new Headers.Builder().add("token", "abc").build();
        Response response = client.newCall(new Request.Builder().get().headers(headers).url("https://127.0.0.1:8080/test/v1/client/get?name=fkp").build()).execute();
        System.out.println(response);
        ResponseBody body = response.body();
        System.out.println(body.string());
    }

    @Test
    void testGet(){
        Map<String, Object> map = okHttpUtils.get("https://127.0.0.1:8080/test/v1/client/get", JSONObject.of("name", "fkp"), JSONObject.of("token", "a1234a"));
        System.out.println(map);
    }

    @Test
    void testPostUrlEncoded(){
        Map<String, Object> map = okHttpUtils.postUrlEncoded("https://127.0.0.1:8080/test/v1/client/postUrlEncoded",
                JSONObject.of("name", "fkp", "age", 25), JSONObject.of("token", "a1234a"));
        System.out.println(map);
    }

    @Test
    void testPostFormData(){
        File file = new File("pom.xml");
        Map<String, Object> map = okHttpUtils.postFormData("https://127.0.0.1:8080/test/v1/client/postFormData",
                JSONObject.of("name", "fkp", "age", 25, "file", file), JSONObject.of("token", "a1234a"));
        System.out.println(map);
    }

    @Test
    void testPostJson(){
        Map<String, Object> map = okHttpUtils.postJson("https://127.0.0.1:8080/test/v1/client/postJson",
                JSONObject.of("name", "fkp", "age", 25), JSONObject.of("token", "a1234a"));
        System.out.println(map);
    }

}
