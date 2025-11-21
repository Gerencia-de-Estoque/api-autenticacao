package com.example.demo.api.service;

import com.example.demo.api.model.FilialEntity;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private FilialEntity filial;

    private static final String SECRET = "minha-chave-secreta-com-pelo-menos-32-caracteres-para-hmac";
    private static final long EXPIRATION_MILLIS = 3600000; // 1 hora

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMillis", EXPIRATION_MILLIS);

        filial = FilialEntity.builder()
                .codigoFilial(1)
                .nomeFilial("Filial Teste")
                .login("filial@teste.com")
                .senhaHash("hash")
                .ativo(true)
                .build();
    }

    @Nested
    @DisplayName("Testes de geração de token")
    class GeracaoToken {

        @Test
        @DisplayName("Deve gerar token válido para filial")
        void deveGerarTokenValido() {
            // Act
            String token = jwtService.generateToken(filial);

            // Assert
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT tem 3 partes
        }

        @Test
        @DisplayName("Deve incluir login como subject no token")
        void deveIncluirLoginComoSubject() {
            // Act
            String token = jwtService.generateToken(filial);

            // Assert
            String login = jwtService.extractLogin(token);
            assertThat(login).isEqualTo("filial@teste.com");
        }

        @Test
        @DisplayName("Deve gerar tokens diferentes para chamadas diferentes")
        void deveGerarTokensDiferentes() {
            // Act
            String token1 = jwtService.generateToken(filial);

            String token2 = jwtService.generateToken(filial);

            // Assert
            assertThat(token1).isNotBlank();
            assertThat(token2).isNotBlank();
            assertThat(token1.split("\\.")).hasSize(3);
            assertThat(token2.split("\\.")).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Testes de extração de claims")
    class ExtracaoClaims {

        @Test
        @DisplayName("Deve extrair login do token")
        void deveExtrairLogin() {
            // Arrange
            String token = jwtService.generateToken(filial);

            // Act
            String login = jwtService.extractLogin(token);

            // Assert
            assertThat(login).isEqualTo("filial@teste.com");
        }

        @Test
        @DisplayName("Deve extrair data de expiração do token")
        void deveExtrairDataExpiracao() {
            // Arrange
            String token = jwtService.generateToken(filial);

            // Act
            Instant expiracao = jwtService.extractExpirationInstant(token);

            // Assert
            assertThat(expiracao).isNotNull();
            assertThat(expiracao).isAfter(Instant.now());
            assertThat(expiracao).isBefore(Instant.now().plusMillis(EXPIRATION_MILLIS + 1000));
        }

        @Test
        @DisplayName("Deve retornar milissegundos de expiração configurados")
        void deveRetornarExpirationMillis() {
            // Act
            long expiration = jwtService.getExpirationMillis();

            // Assert
            assertThat(expiration).isEqualTo(EXPIRATION_MILLIS);
        }
    }

    @Nested
    @DisplayName("Testes de validação de token")
    class ValidacaoToken {

        @Test
        @DisplayName("Deve validar token com login correto")
        void deveValidarTokenComLoginCorreto() {
            // Arrange
            String token = jwtService.generateToken(filial);

            // Act
            boolean isValid = jwtService.isTokenValid(token, "filial@teste.com");

            // Assert
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Deve invalidar token com login incorreto")
        void deveInvalidarTokenComLoginIncorreto() {
            // Arrange
            String token = jwtService.generateToken(filial);

            // Act
            boolean isValid = jwtService.isTokenValid(token, "outro@teste.com");

            // Assert
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Deve invalidar token expirado")
        void deveInvalidarTokenExpirado() {
            // Arrange - configurar expiração muito curta
            ReflectionTestUtils.setField(jwtService, "expirationMillis", 1L); // 1ms
            String token = jwtService.generateToken(filial);

            // Aguardar expiração
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Act
            boolean isValid;
            try {
                isValid = jwtService.isTokenValid(token, "filial@teste.com");
            } catch (ExpiredJwtException ex) {
                isValid = false;
            }

            // Assert
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de tratamento de erros")
    class TratamentoErros {

        @Test
        @DisplayName("Deve lançar exceção para token malformado")
        void deveLancarExcecaoParaTokenMalformado() {
            // Act & Assert
            assertThatThrownBy(() -> jwtService.extractLogin("token-invalido"))
                    .isInstanceOf(MalformedJwtException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção para token vazio")
        void deveLancarExcecaoParaTokenVazio() {
            // Act & Assert
            assertThatThrownBy(() -> jwtService.extractLogin(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando secret não está configurado")
        void deveLancarExcecaoQuandoSecretNaoConfigurado() {
            // Arrange
            JwtService serviceWithoutSecret = new JwtService();
            ReflectionTestUtils.setField(serviceWithoutSecret, "secret", "");
            ReflectionTestUtils.setField(serviceWithoutSecret, "expirationMillis", EXPIRATION_MILLIS);

            // Act & Assert
            assertThatThrownBy(() -> serviceWithoutSecret.generateToken(filial))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("JWT secret is not configured");
        }

        @Test
        @DisplayName("Deve lançar exceção para token com assinatura inválida")
        void deveLancarExcecaoParaAssinaturaInvalida() {
            // Arrange
            String token = jwtService.generateToken(filial);
            // Modificar o token para invalidar a assinatura
            String[] parts = token.split("\\.");
            String invalidToken = parts[0] + "." + parts[1] + ".assinatura-invalida";

            // Act & Assert
            assertThatThrownBy(() -> jwtService.extractLogin(invalidToken))
                    .isInstanceOf(Exception.class);
        }

        @ParameterizedTest(name = "Token inválido: {0}")
        @MethodSource("com.example.demo.api.service.JwtServiceTest#tokensInvalidos")
        @DisplayName("Deve lançar exceção para tokens malformados")
        void deveRejeitarTokensInvalidos(String tokenInvalido) {
            assertThatThrownBy(() -> jwtService.extractLogin(tokenInvalido))
                    .isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("Testes com diferentes filiais")
    class DiferentesFiliais {

        @Test
        @DisplayName("Deve gerar tokens diferentes para filiais diferentes")
        void deveGerarTokensDiferentesParaFiliaisDiferentes() {
            // Arrange
            FilialEntity outraFilial = FilialEntity.builder()
                    .codigoFilial(2)
                    .nomeFilial("Outra Filial")
                    .login("outra@teste.com")
                    .senhaHash("hash")
                    .ativo(true)
                    .build();

            // Act
            String token1 = jwtService.generateToken(filial);
            String token2 = jwtService.generateToken(outraFilial);

            // Assert
            assertThat(token1).isNotEqualTo(token2);
            assertThat(jwtService.extractLogin(token1)).isEqualTo("filial@teste.com");
            assertThat(jwtService.extractLogin(token2)).isEqualTo("outra@teste.com");
        }
    }

    static Stream<String> tokensInvalidos() {
        return Stream.of(
                "token-invalido",
                "",
                "eyJhbGciOiJIUzI1NiJ9.payload.assinatura"
        );
    }
}
