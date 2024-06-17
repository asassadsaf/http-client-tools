package com.fkp.tools.okhttp3;

import lombok.SneakyThrows;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Okhttp3ApplicationTests {

    @Autowired
    private OkHttpClient client;

    @SneakyThrows
    @Test
    void contextLoads() {
        Headers headers = new Headers.Builder().add("token", "abc").build();
        Response response = client.newCall(new Request.Builder().get().headers(headers).url("https://127.0.0.1:8080/test/v1/client/get?name=fkp").build()).execute();
        System.out.println(response);
        ResponseBody body = response.body();
        System.out.println(body.string());
    }

}
