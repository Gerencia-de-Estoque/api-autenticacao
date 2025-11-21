package com.example.demo.api;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.api.model.FilialEntity;
import com.example.demo.api.repository.FilialRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FilialRepository filialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void prepararBanco() {
        filialRepository.deleteAll();
        FilialEntity filial = FilialEntity.builder()
                .nomeFilial("Filial Auth Test")
                .login("auth@test.com")
                .senhaHash(passwordEncoder.encode("password"))
                .ativo(true)
                .build();
        filialRepository.save(filial);
    }

    @Test
    void deveAutenticarUsuarioValido() throws Exception {
        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"login":"auth@test.com","senha":"password"}
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEm", notNullValue()));
    }

    @Test
    void deveNegarSenhaIncorreta() throws Exception {
        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"login":"auth@test.com","senha":"errada"}
                                        """)
                )
                .andExpect(status().isUnauthorized());
    }
}
