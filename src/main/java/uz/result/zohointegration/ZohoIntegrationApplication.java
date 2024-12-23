package uz.result.zohointegration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ZohoIntegrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZohoIntegrationApplication.class, args);
    }

}
