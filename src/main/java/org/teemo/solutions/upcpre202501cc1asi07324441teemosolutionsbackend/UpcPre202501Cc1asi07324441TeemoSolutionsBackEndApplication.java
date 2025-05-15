package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.infrastructure.configuration.JwtProperties;

@SpringBootApplication(scanBasePackages = "org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend")
@EnableMongoRepositories
@EnableMongoAuditing
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class UpcPre202501Cc1asi07324441TeemoSolutionsBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(UpcPre202501Cc1asi07324441TeemoSolutionsBackEndApplication.class, args);
	}
}