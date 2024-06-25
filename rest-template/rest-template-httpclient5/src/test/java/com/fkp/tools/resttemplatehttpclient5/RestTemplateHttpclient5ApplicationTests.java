package com.fkp.tools.resttemplatehttpclient5;

import com.fkp.tools.resttemplatehttpclient5.config.RestTemplateConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class RestTemplateHttpclient5ApplicationTests {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void testGet() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token", "abc");
        HttpEntity<?> httpEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> forEntity = restTemplate.exchange("https://127.0.0.1:8080/test/v1/client/get?name=fkp",
                HttpMethod.GET, httpEntity, String.class);
        System.out.println(forEntity.getBody());
    }

    @Test
    void testPostUrlEncoded(){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token", "abc");
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("name", "fkp");
        body.add("age", 25);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange("https://127.0.0.1:8080/test/v1/client/postUrlEncoded",
                HttpMethod.POST, httpEntity, String.class);
        System.out.println(response.getBody());
    }

    @Test
    void testPostFormData(){
        File file = new File("pom.xml");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token", "abc");
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("name", "fkp");
        body.add("age", "23");
        body.add("file", new FileSystemResource(file));
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(body, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange("https://127.0.0.1:8080/test/v1/client/postFormData",
                HttpMethod.POST, httpEntity, String.class);
        System.out.println(response.getBody());
    }

    @Test
    void testJson(){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token", "abc");
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = new HashMap<>();
        body.put("name", "fkp");
        body.put("age", 25);
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange("https://127.0.0.1:8080/test/v1/client/postJson",
                HttpMethod.POST, httpEntity, String.class);
        System.out.println(response.getBody());
    }

}
