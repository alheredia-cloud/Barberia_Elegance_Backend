package cl.duoc.barberia.service;

import cl.duoc.barberia.dto.AuthDto.*;
import cl.duoc.barberia.exception.BusinessException;
import cl.duoc.barberia.model.Rol;
import cl.duoc.barberia.model.Usuario;
import cl.duoc.barberia.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    /** Verifica credenciales (BCrypt) mediante Spring Security y emite el par de tokens. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        return emitirTokens(usuarioService.buscarPorEmail(email));
    }

    /** Registro publico: siempre crea un CLIENTE (el rol no se acepta desde el frontend). */
    @Transactional
    public AuthResponse registrar(RegisterRequest req) {
        Usuario u = usuarioService.registrar(req.nombre(), req.rut(), req.email(), req.password(), Rol.CLIENTE);
        return emitirTokens(u);
    }

    /** Renueva el access token a partir de un refresh token valido (rotacion: se entrega un refresh nuevo). */
    @Transactional(readOnly = true)
    public AuthResponse refrescar(RefreshRequest req) {
        String email;
        try {
            email = jwtService.validar(req.refreshToken(), JwtService.TIPO_REFRESH).getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException("Refresh token invalido o expirado", HttpStatus.UNAUTHORIZED);
        }
        Usuario u = usuarioService.buscarPorEmail(email);
        if (!u.isActivo()) {
            throw new BusinessException("La cuenta se encuentra deshabilitada", HttpStatus.UNAUTHORIZED);
        }
        return emitirTokens(u);
    }

    @Transactional(readOnly = true)
    public UsuarioInfo perfil(String email) {
        return toInfo(usuarioService.buscarPorEmail(email));
    }

    private AuthResponse emitirTokens(Usuario u) {
        return new AuthResponse(
                jwtService.generarAccessToken(u),
                jwtService.generarRefreshToken(u),
                "Bearer",
                jwtService.getAccessExpirationMs() / 1000,
                toInfo(u));
    }

    private static UsuarioInfo toInfo(Usuario u) {
        return new UsuarioInfo(u.getId(), u.getNombre(), u.getEmail(), u.getRut(), u.getRol());
    }
}
