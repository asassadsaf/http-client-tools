package com.fkp.tools.httpclient5;

import com.alibaba.fastjson2.JSONObject;
import com.fkp.tools.httpclient5.util.HttpClient5Utils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest
class Httpclient5ApplicationTests {

    @Autowired
    private HttpClient5Utils httpClient5Utils;

    @Test
    void testGet(){
        Map<String, Object> map = httpClient5Utils.get("https://127.0.0.1:8080/test/v1/client/get", JSONObject.of("name", "fkp"), JSONObject.of("token", "a1234a"));
        System.out.println(map);
    }

    @Test
    void testPostUrlEncoded(){
        Map<String, Object> map = httpClient5Utils.postUrlEncoded("https://127.0.0.1:8080/test/v1/client/postUrlEncoded",
                JSONObject.of("name", "fkp", "age", 25), JSONObject.of("token", "a1234a"));
        System.out.println(map);
    }

    @Test
    void testPostFormData(){
        File file = new File("pom.xml");
        Map<String, Object> map = httpClient5Utils.postFormData("https://127.0.0.1:8080/test/v1/client/postFormData",
                JSONObject.of("name", "fkp", "age", 25, "file", file), JSONObject.of("token", "a1234a"));
        System.out.println(map);
    }

    @Test
    void testPostJson(){
        Map<String, Object> map = httpClient5Utils.postJson("https://127.0.0.1:8080/test/v1/client/postJson",
                JSONObject.of("name", "fkp", "age", 25), JSONObject.of("token", "a1234a"));
        System.out.println(map);
    }

    @SneakyThrows
    @Test
    void contextLoads() {

        for (int i = 0; i <1000; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    Map<String, Object> map = httpClient5Utils.get("https://10.0.104.191:8080/test/v1/system/readClassPathFile");
                    System.out.println(map);
                }
            });
            thread.start();
        }
        Thread.currentThread().join();
    }

    void testKeepAlive(String[] args){
        AtomicLong COUNT = new AtomicLong();
        int threadNum = Integer.parseInt(args[0]);
        String url = args[1];

        for (int i = 0; i <threadNum; i++) {
            new Thread(() -> {
                while (true) {
                    httpClient5Utils.get(url);
                    COUNT.incrementAndGet();
                }
            }).start();
        }
        new Thread(() -> {
            int time = 0;
            while (true){
                try {
                    Thread.sleep(1000);
                    time+=1;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(COUNT.get()/time);
            }
        }).start();
    }

}
