package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // ➜ Habilita o agendamento
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	// Se for usar deploy em WAR, descomente este método:
	// @Override
	// protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
	//     return builder.sources(DemoApplication.class);
	// }
}
