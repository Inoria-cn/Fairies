package org.squirrelnest.fairies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by Inoria on 2019/3/6.
 */
@SpringBootApplication
public class Application {

    public static void Main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.run(args);
    }
}
