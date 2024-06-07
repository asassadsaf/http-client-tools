package com.fkp.tools.httpclient5;

import com.fkp.tools.httpclient5.util.HttpClient5Utils;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.atomic.AtomicLong;

@SpringBootApplication
public class Httpclient5Application {

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(Httpclient5Application.class, args);
    }

}
