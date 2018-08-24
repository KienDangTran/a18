package com.a18.auth;

import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.a18.auth", "com.a18.common"})
@SpringBootApplication
public class AuthServerApplication {
  @Value("${timezone}")
  private String timezone = "Asia/Ho_Chi_Minh";

  public static void main(String[] args) {
    SpringApplication.run(AuthServerApplication.class, args);
  }

  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone(timezone));
  }
}
