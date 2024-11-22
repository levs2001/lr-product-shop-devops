package ru.lr.shop;

import java.io.IOException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import ru.lr.shop.sys.TerminalUtils;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws IOException {
//        TerminalUtils.executeDetach(
//            "docker-compose", "-f", "./src/main/resources/dev_cluster/docker-compose.yml", "up", "--force-recreate", "-V", "-d"
//        );
        SpringApplication.run(Application.class, args);
    }
}
