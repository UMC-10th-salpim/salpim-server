package salpim.umc10thsalpim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SalpimApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalpimApplication.class, args);
	}

}
