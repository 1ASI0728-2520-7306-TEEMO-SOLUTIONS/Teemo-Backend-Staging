// java
package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.shared.infrastructure.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "authorization.jwt")
public class JwtProperties {
    private String secret;
    private Expiration expiration;

    @Getter
    @Setter
    public static class Expiration {
        private int days;
    }
}
