package com.jn.sqlhelper.examples.apache.dbutils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.jn.sqlhelper.examples.apache.dbutils",
        "com.jn.sqlhelper.examples.db.config"
})
public class ApacheDbUtilsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApacheDbUtilsApplication.class, args);
    }
}
