package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.application.internal.outboundservices.tokens;

public interface TokenService {
    String generateToken(String username);
    String getUsernameFromToken(String token);
    boolean validateToken(String token);
}
