package cl.duoc.barberia;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integracion: levantan la aplicacion completa con H2 y verifican
 * autenticacion JWT, renovacion de tokens y permisos por rol sobre la API REST.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class SeguridadIntegracionTest {

    @Autowired
    private MockMvc mvc;

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    @Test
    void catalogoEsPublico() throws Exception {
        mvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").exists());
    }

    @Test
    void loginConClaveIncorrectaDevuelve401() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@barberia.cl\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginDevuelveTokensYRol() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@barberia.cl\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.usuario.rol").value("ADMIN"));
    }

    @Test
    void sinTokenNoAccedeARecursoProtegido() throws Exception {
        mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenManipuladoEsRechazado() throws Exception {
        String token = login("cliente@barberia.cl", "cliente123");
        // Se reemplaza el payload por uno que dice rol ADMIN, conservando la firma original
        String[] partes = token.split("\\.");
        String payloadFalso = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"cliente@barberia.cl\",\"rol\":\"ADMIN\",\"tipo\":\"access\",\"iss\":\"barberia-elegance-api\",\"exp\":9999999999}"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String tokenFalso = partes[0] + "." + payloadFalso + "." + partes[2];
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + tokenFalso))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Token invalido"));
    }

    @Test
    void refreshTokenEntregaNuevoAccessToken() throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cliente@barberia.cl\",\"password\":\"cliente123\"}"))
                .andReturn().getResponse().getContentAsString();
        String refresh = JsonPath.read(body, "$.refreshToken");
        String access = JsonPath.read(body, "$.accessToken");

        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        // Un access token no sirve como refresh token
        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + access + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeVerBoletasNiUsuarios() throws Exception {
        String token = login("cliente@barberia.cl", "cliente123");
        mvc.perform(get("/api/v1/boletas").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorVeBoletasPeroNoAdministra() throws Exception {
        String token = login("vendedor@barberia.cl", "vendedor123");
        mvc.perform(get("/api/v1/boletas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/productos/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminAccedeATodo() throws Exception {
        String token = login("admin@barberia.cl", "admin123");
        mvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/dashboard/resumen").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsuarios").isNumber());
    }

    @Test
    void registroPublicoSiempreCreaCliente() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo\",\"rut\":\"33333333-3\",\"email\":\"nuevo@correo.cl\",\"password\":\"clave123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"));
    }

    @Test
    void checkoutCalculaTotalEnServidorYDescuentaStock() throws Exception {
        String token = login("cliente@barberia.cl", "cliente123");
        // Producto 6 = Cera Modeladora (8000, stock 15)
        mvc.perform(post("/api/v1/boletas").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreCliente\":\"Juan\",\"metodoPago\":\"debito\",\"items\":[{\"productoId\":6,\"cantidad\":2},{\"productoId\":1,\"cantidad\":1}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(31000))
                .andExpect(jsonPath("$.requiereCita").value(true));
        mvc.perform(get("/api/v1/productos/6"))
                .andExpect(jsonPath("$.stock").value(13));
    }
}
