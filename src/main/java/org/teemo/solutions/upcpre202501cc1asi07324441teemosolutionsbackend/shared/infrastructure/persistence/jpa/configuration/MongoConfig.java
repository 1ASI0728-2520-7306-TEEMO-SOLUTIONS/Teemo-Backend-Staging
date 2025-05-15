package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.infrastructure.persistence.jpa.configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    private MongoClient mongoClient;

    @Bean
    public MongoClient mongoClient() {
        this.mongoClient = MongoClients.create(mongoUri);
        return this.mongoClient;
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        LOGGER.info("Using database: {}", databaseName);
        return new MongoTemplate(mongoClient(), databaseName);
    }


    @PreDestroy
    public void closeMongoClient() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
