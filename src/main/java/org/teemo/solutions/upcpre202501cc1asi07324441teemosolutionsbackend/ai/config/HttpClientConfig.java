package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HttpClientConfig {

    @Bean
    public WebClient webClient() {
        // incrementa buffer por si la respuesta trae arrays horarios grandes
        var strategies = ExchangeStrategies.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        return WebClient.builder()
                .exchangeStrategies(strategies)
                .build();
    }
}
