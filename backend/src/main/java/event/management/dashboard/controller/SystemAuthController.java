package event.management.dashboard.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4000")
public class SystemAuthController {

    private final String SECRET_STRING = "mySecretKeyMustBeAtLeast32CharactersLongForHS256!";
    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    @PostMapping("/system-login")
    public ResponseEntity<?> systemLogin(@RequestBody Map<String, String> payload) {
        if ("dashboard_system".equals(payload.get("username")) && "system_secure_pass_2026".equals(payload.get("password"))) {

            String token = Jwts.builder()
                    .subject("SystemDashboard")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 86400000)) // Валиден 24 часа
                    .signWith(secretKey)
                    .compact();

            return ResponseEntity.ok(Map.of("token", token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
