package com.fkp.tools.webclient;

import com.alibaba.fastjson2.JSONObject;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;

@SpringBootTest
class WebclientApplicationTests {

    @Autowired
    private WebClient webClient;

    @SneakyThrows
    @Test
    void testGet() {
        Mono<Object> objectMono = webClient
                .get()
                .uri("https://127.0.0.1:8080/test/v1/client/get?name=fkp")
                .header("token", "abc")
                .retrieve()
                .bodyToMono(Object.class);
//        Object block = objectMono.block();
//        System.out.println(block);
        objectMono.subscribe(System.out::println);
        //使主线程不停止，否则无法正常获取响应
        Thread.currentThread().join();
    }

    @SneakyThrows
    @Test
    void testPostJson(){
        Mono<String> objectMono = webClient
                .post()
                .uri("https://127.0.0.1:8080/test/v1/client/postJson")
                .body(BodyInserters.fromValue(JSONObject.of("name", "fkp", "age", 25)))
                .header("token", "abc")
                .retrieve()
                .bodyToMono(String.class);
        objectMono.subscribe(System.out::println);
        Thread.currentThread().join();
    }

    @SneakyThrows
    @Test
    void testPostUrlEncoded(){
        Mono<String> objectMono = webClient
                .post()
                .uri("https://127.0.0.1:8080/test/v1/client/postUrlEncoded")
                .body(BodyInserters.fromFormData("name", "fkp").with("age", "25"))
                .header("token", "abc")
                .headers(header -> header.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
                .retrieve()
                .bodyToMono(String.class);
        objectMono.subscribe(System.out::println);
        Thread.currentThread().join();
    }

    @SneakyThrows
    @Test
    void testPostUrlFormData(){
        File file = new File("pom.xml");
        Mono<String> objectMono = webClient
                .post()
                .uri("https://127.0.0.1:8080/test/v1/client/postFormData")
                .body(BodyInserters.fromMultipartData("name", "fkp").with("age", "25").with("file", new FileSystemResource(file.getAbsolutePath())))
                .header("token", "abc")
//                .headers(header -> header.setContentType(MediaType.MULTIPART_FORM_DATA))
                .retrieve()
                .bodyToMono(String.class);
        objectMono.subscribe(System.out::println);
        Thread.currentThread().join();
    }

}
