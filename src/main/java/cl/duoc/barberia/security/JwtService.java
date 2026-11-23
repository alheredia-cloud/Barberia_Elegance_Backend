package cl.duoc.barberia.security;

import cl.duoc.barberia.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Generacion y verificacion de tokens JWT firmados con HMAC-SHA256.
 * Se emiten dos tokens: access (vida corta, va en cada request) y refresh (vida larga, solo sirve para renovar).
 */
@Service
public class JwtService {

    public static final String CLAIM_TIPO = "tipo";
    public static final String CLAIM_ROL = "rol";
    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs,
                      @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generarAccessToken(Usuario u) {
        return Jwts.builder()
                .subject(u.getEmail())
                .claim(CLAIM_ROL, u.getRol().name())
                .claim("nombre", u.getNombre())
                .claim(CLAIM_TIPO, TIPO_ACCESS)
                .issuer("barberia-elegance-api")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(key)
                .compact();
    }

    public String generarRefreshToken(Usuario u) {
        return Jwts.builder()
                .subject(u.getEmail())
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_TIPO, TIPO_REFRESH)
                .issuer("barberia-elegance-api")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Verifica firma y expiracion. Lanza {@link JwtException} si el token es invalido,
     * fue manipulado o expiro.
     */
    public Claims validar(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer("barberia-elegance-api")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims validar(String token, String tipoEsperado) {
        Claims claims = validar(token);
        if (!tipoEsperado.equals(claims.get(CLAIM_TIPO, String.class))) {
            throw new JwtException("Tipo de token incorrecto");
        }
        return claims;
    }

    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }
}
