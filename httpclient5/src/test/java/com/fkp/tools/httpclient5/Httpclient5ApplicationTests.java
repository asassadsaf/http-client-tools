package com.fkp.tools.httpclient5;

import com.fkp.tools.httpclient5.util.HttpClient5Utils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest
class Httpclient5ApplicationTests {

    @Autowired
    private HttpClient5Utils httpClient5Utils;

    @Test
    void testGet(){
        Map<String, Object> map = httpClient5Utils.get("http://10.0.104.191:8080/test/v1/system/readClassPathFile");
        System.out.println(map);
    }

    @Test
    void testPost(){
        Map<String, Object> post = httpClient5Utils.postJson("https://10.0.104.191:8080/test/v1/health");
        System.out.println(post);
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
