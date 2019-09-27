package com.akka.wrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ImportsPlatformApplication {

    private static Class<ImportsPlatformApplication> applicationClass = ImportsPlatformApplication.class;

    public static void main(String[] args) {
        SpringApplication.run(applicationClass, args);
    }

    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }

}
