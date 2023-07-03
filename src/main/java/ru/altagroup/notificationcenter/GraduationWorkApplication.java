package ru.altagroup.notificationcenter;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info =
@Info(title = "Notification-center API", version = "1.0", description = "Documentation notification-center API v1.0"))
public class GraduationWorkApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraduationWorkApplication.class, args);
	}

}
