package com.example.demo.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.api.dto.LoginRequest;
import com.example.demo.api.dto.LoginResponse;
import com.example.demo.api.security.JwtAuthenticationFilter;
import com.example.demo.api.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Nested
    @DisplayName("POST /auth/login")
    class LoginEndpoint {

        @Test
        @DisplayName("Deve retornar 200 e token para credenciais válidas")
        void deveRetornar200ParaCredenciaisValidas() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("user@test.com", "password");
            LoginResponse response = new LoginResponse(
                    "jwt-token",
                    "Bearer",
                    Instant.parse("2024-12-31T23:59:59Z")
            );

            when(authService.autenticar(any(LoginRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.tipo").value("Bearer"))
                    .andExpect(jsonPath("$.expiraEm").exists());
        }

        @Test
        @DisplayName("Deve retornar 401 para credenciais inválidas")
        void deveRetornar401ParaCredenciaisInvalidas() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("user@test.com", "wrong");

            when(authService.autenticar(any(LoginRequest.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login ou senha invalidos"));

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Deve retornar 403 para filial desativada")
        void deveRetornar403ParaFilialDesativada() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("inativo@test.com", "password");

            when(authService.autenticar(any(LoginRequest.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Filial desativada"));

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Deve aceitar requisição com Content-Type application/json")
        void deveAceitarContentTypeJson() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("user@test.com", "password");
            LoginResponse response = new LoginResponse("token", "Bearer", Instant.now());

            when(authService.autenticar(any(LoginRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve retornar resposta com estrutura correta")
        void deveRetornarRespostaComEstruturaCorreta() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("user@test.com", "password");
            Instant expiraEm = Instant.now().plusSeconds(3600);
            LoginResponse response = new LoginResponse("my-jwt-token", "Bearer", expiraEm);

            when(authService.autenticar(any(LoginRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isString())
                    .andExpect(jsonPath("$.tipo").isString())
                    .andExpect(jsonPath("$.expiraEm").isString());
        }
    }

    @Nested
    @DisplayName("Testes de validação de entrada")
    class ValidacaoEntrada {

        @Test
        @DisplayName("Deve processar requisição com login e senha vazios")
        void deveProcessarComLoginESenhaVazios() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("", "");

            when(authService.autenticar(any(LoginRequest.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login ou senha invalidos"));

            // Act & Assert
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
