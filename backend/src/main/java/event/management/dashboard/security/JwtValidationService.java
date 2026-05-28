package event.management.dashboard.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtValidationService {

    // IMPORTANT: In production, load this from application.yml via @Value("${jwt.secret}")
    // The secret string must be at least 32 characters long for the HS256 algorithm.
    private final String SECRET_STRING = "mySecretKeyMustBeAtLeast32CharactersLongForHS256!";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    /**
     * Валидира дали токенът е автентичен, дали не е подправен и дали е все още в период на валидност.
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Допълнителна експлицитна проверка дали токенът е изтекъл
            return !claims.getExpiration().before(new Date());

        } catch (ExpiredJwtException e) {
            System.err.println("JWT Validation Error: Token is expired. " + e.getMessage());
            return false;
        } catch (MalformedJwtException | SignatureException e) {
            System.err.println("JWT Validation Error: Invalid token " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("JWT Validation Error: Error during validation " + e.getMessage());
            return false;
        }
    }

    /**
     * Извлича потребителското име (Subject) от JWT токена.
     * Този метод се вика само след като токенът вече е валидиран.
     */
    public String extractUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            System.err.println("Error extracting username from JWT: " + e.getMessage());
            return null;
        }
    }
}
