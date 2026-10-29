package online.stworzgrafik.StworzGrafik;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StworzGrafikApplication {
	public static void main(String[] args) {
		SpringApplication.run(StworzGrafikApplication.class, args);
	}
}
