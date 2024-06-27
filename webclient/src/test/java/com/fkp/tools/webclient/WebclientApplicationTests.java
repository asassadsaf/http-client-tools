package com.fkp.tools.webclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@SpringBootTest
class WebclientApplicationTests {

    @Autowired
    private WebClient webClient;

    @Test
    void contextLoads() {
        Mono<Object> objectMono = webClient.get().uri("https://127.0.0.1:8080/test/v1/client/get?name=fkp").retrieve().bodyToMono(Object.class);
        Object block = objectMono.block();
        System.out.println(block);
    }

}
