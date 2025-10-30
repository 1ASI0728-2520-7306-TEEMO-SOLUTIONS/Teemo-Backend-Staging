package org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.infrastructure.tokens.jwt.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.teemo.solutions.upcpre202501cc1asi07324441teemosolutionsbackend.iam.infrastructure.tokens.jwt.BearerTokenService;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class TokenServiceImpl implements BearerTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int TOKEN_BEGIN_INDEX = BEARER_PREFIX.length();

    private final SecretKey key;

    @Value("${authorization.jwt.secret}")
    private String secret;

    @Value("${authorization.jwt.expiration.days}")
    private int expirationDays;

    @Autowired
    public TokenServiceImpl(@Value("${authorization.jwt.secret}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("The JWT secret must be at least 256 bits (32 bytes).");
        }
        this.secret = secret;
        this.key = Keys.hmacShaKeyFor(secret.getBytes()); // Convert secret to byte array
    }


    /**
     * Extract the Bearer token from the HTTP request.
     *
     * @param request HttpServletRequest containing the token in the Authorization header.
     * @return Extracted token or null if not present or improperly formatted.
     */
    @Override
    public String getBearerTokenFrom(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (isTokenPresent(authHeader) && isBearerToken(authHeader)) {
            return extractToken(authHeader);
        }
        LOGGER.warn("Token is not present or improperly formatted in Authorization header.");
        return null;
    }

    /**
     * Generate a token using an authenticated user.
     *
     * @param authentication The Authentication object containing the user's details.
     * @return A signed JWT token.
     */
    @Override
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        LOGGER.info("Generating token for user: {}", username);
        return buildToken(username);
    }

    /**
     * Generate a token using a specific username.
     *
     * @param username The username for which to generate the token.
     * @return A signed JWT token.
     */
    @Override
    public String generateToken(String username) {
        LOGGER.info("Generating token for username: {}", username);
        return buildToken(username);
    }

    /**
     * Validate a JWT token.
     *
     * @param token The token to validate.
     * @return True if the token is valid, false otherwise.
     */
    @Override
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            LOGGER.info("Token is valid.");
            return true;
        } catch (ExpiredJwtException e) {
            LOGGER.error("Token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            LOGGER.error("Unsupported token: {}", e.getMessage());
        } catch (JwtException e) {
            LOGGER.error("Invalid token: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extract the username (subject) from the token.
     *
     * @param token The JWT token.
     * @return The username extracted from the token.
     */
    @Override
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ======================== PRIVATE METHODS ============================

    /**
     * Build a JWT token with the given username.
     *
     * @param username The username to embed in the token.
     * @return A signed JWT token.
     */
    private String buildToken(String username) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationDays * 86400000L); // Convert days to milliseconds

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256) // Use HS256 algorithm
                .compact();
    }

    /**
     * Extract all claims from the JWT token.
     *
     * @param token The JWT token.
     * @return The claims extracted from the token.
     */
    private Claims extractAllClaims(String token) {
        return parseToken(token).getBody();
    }

    /**
     * Extract a specific claim from the token using a resolver function.
     *
     * @param token          The JWT token.
     * @param claimsResolver A function to extract a specific claim.
     * @param <T>            The type of the claim.
     * @return The extracted claim.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Check if the Authorization header is present and has text.
     *
     * @param authHeader The Authorization header.
     * @return True if the header is present and non-empty, false otherwise.
     */
    private boolean isTokenPresent(String authHeader) {
        return StringUtils.hasText(authHeader);
    }

    /**
     * Check if the Authorization header starts with "Bearer ".
     *
     * @param authHeader The Authorization header.
     * @return True if the header starts with "Bearer ", false otherwise.
     */
    private boolean isBearerToken(String authHeader) {
        return authHeader.startsWith(BEARER_PREFIX);
    }

    /**
     * Extract the token from the Authorization header by removing the "Bearer " prefix.
     *
     * @param authHeader The Authorization header.
     * @return The token string.
     */
    private String extractToken(String authHeader) {
        return authHeader.substring(TOKEN_BEGIN_INDEX);
    }

    /**
     * Parse a token and validate its signature.
     *
     * @param token The JWT token to parse.
     * @return Parsed claims if the token is valid.
     */
    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(key) // Use the key for verification
                .build()
                .parseClaimsJws(token); // Parse the claims
    }
}
