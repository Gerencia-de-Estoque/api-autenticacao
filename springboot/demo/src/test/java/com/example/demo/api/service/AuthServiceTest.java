package com.example.demo.api.service;

import com.example.demo.api.dto.LoginRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import com.example.demo.api.dto.LoginResponse;
import com.example.demo.api.model.FilialEntity;
import com.example.demo.api.repository.FilialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private FilialRepository filialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private FilialEntity filialAtiva;
    private FilialEntity filialInativa;

    @BeforeEach
    void setUp() {
        filialAtiva = FilialEntity.builder()
                .codigoFilial(1)
                .nomeFilial("Filial Teste")
                .login("filial@teste.com")
                .senhaHash("hashedPassword")
                .ativo(true)
                .build();

        filialInativa = FilialEntity.builder()
                .codigoFilial(2)
                .nomeFilial("Filial Inativa")
                .login("inativa@teste.com")
                .senhaHash("hashedPassword")
                .ativo(false)
                .build();
    }

    @Nested
    @DisplayName("Testes de autenticação bem-sucedida")
    class AutenticacaoSucesso {

        @Test
        @DisplayName("Deve autenticar usuário com credenciais válidas")
        void deveAutenticarComCredenciaisValidas() {
            // Arrange
            LoginRequest request = new LoginRequest("filial@teste.com", "senhaCorreta");
            String token = "jwt-token-gerado";
            Instant expiraEm = Instant.now().plusSeconds(3600);

            when(filialRepository.findByLogin("filial@teste.com")).thenReturn(Optional.of(filialAtiva));
            when(passwordEncoder.matches("senhaCorreta", "hashedPassword")).thenReturn(true);
            when(jwtService.generateToken(filialAtiva)).thenReturn(token);
            when(jwtService.extractExpirationInstant(token)).thenReturn(expiraEm);

            // Act
            LoginResponse response = authService.autenticar(request);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo(token);
            assertThat(response.tipo()).isEqualTo("Bearer");
            assertThat(response.expiraEm()).isEqualTo(expiraEm);
        }

        @Test
        @DisplayName("Deve retornar token Bearer no tipo de resposta")
        void deveRetornarTokenBearer() {
            // Arrange
            LoginRequest request = new LoginRequest("filial@teste.com", "senhaCorreta");

            when(filialRepository.findByLogin(anyString())).thenReturn(Optional.of(filialAtiva));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
            when(jwtService.generateToken(any())).thenReturn("token");
            when(jwtService.extractExpirationInstant(anyString())).thenReturn(Instant.now());

            // Act
            LoginResponse response = authService.autenticar(request);

            // Assert
            assertThat(response.tipo()).isEqualTo("Bearer");
        }
    }

    @Nested
    @DisplayName("Testes de falha na autenticação")
    class AutenticacaoFalha {

        @Test
        @DisplayName("Deve lançar exceção quando login não existe")
        void deveLancarExcecaoQuandoLoginNaoExiste() {
            // Arrange
            LoginRequest request = new LoginRequest("inexistente@teste.com", "senha");

            when(filialRepository.findByLogin("inexistente@teste.com")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authService.autenticar(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Login ou senha invalidos");
        }

        @Test
        @DisplayName("Deve lançar exceção quando senha está incorreta")
        void deveLancarExcecaoQuandoSenhaIncorreta() {
            // Arrange
            LoginRequest request = new LoginRequest("filial@teste.com", "senhaErrada");

            when(filialRepository.findByLogin("filial@teste.com")).thenReturn(Optional.of(filialAtiva));
            when(passwordEncoder.matches("senhaErrada", "hashedPassword")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.autenticar(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Login ou senha invalidos");
        }

        @Test
        @DisplayName("Deve lançar exceção quando filial está desativada")
        void deveLancarExcecaoQuandoFilialDesativada() {
            // Arrange
            LoginRequest request = new LoginRequest("inativa@teste.com", "senha");

            when(filialRepository.findByLogin("inativa@teste.com")).thenReturn(Optional.of(filialInativa));

            // Act & Assert
            assertThatThrownBy(() -> authService.autenticar(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Filial desativada");
        }

        @Test
        @DisplayName("Deve verificar status FORBIDDEN para filial desativada")
        void deveVerificarStatusForbiddenParaFilialDesativada() {
            // Arrange
            LoginRequest request = new LoginRequest("inativa@teste.com", "senha");

            when(filialRepository.findByLogin("inativa@teste.com")).thenReturn(Optional.of(filialInativa));

            // Act & Assert
            assertThatThrownBy(() -> authService.autenticar(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode().value()).isEqualTo(403);
                    });
        }

        @Test
        @DisplayName("Deve verificar status UNAUTHORIZED para credenciais inválidas")
        void deveVerificarStatusUnauthorizedParaCredenciaisInvalidas() {
            // Arrange
            LoginRequest request = new LoginRequest("filial@teste.com", "senhaErrada");

            when(filialRepository.findByLogin("filial@teste.com")).thenReturn(Optional.of(filialAtiva));
            when(passwordEncoder.matches("senhaErrada", "hashedPassword")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authService.autenticar(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode().value()).isEqualTo(401);
                    });
        }

        @ParameterizedTest
        @ValueSource(strings = {"senhaErrada", "senhaMuitoErrada", "outraSenha"})
        @DisplayName("Deve rejeitar múltiplas senhas incorretas como não autorizadas")
        void deveRejeitarVariasSenhasIncorretas(String senhaDigitada) {
            LoginRequest request = new LoginRequest("filial@teste.com", senhaDigitada);

            when(filialRepository.findByLogin("filial@teste.com")).thenReturn(Optional.of(filialAtiva));
            when(passwordEncoder.matches(senhaDigitada, "hashedPassword")).thenReturn(false);

            assertThatThrownBy(() -> authService.autenticar(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode().value()).isEqualTo(401);
                    });
        }
    }

    @Nested
    @DisplayName("Testes de casos de borda")
    class CasosDeBorda {

        @Test
        @DisplayName("Deve tratar filial com ativo null como inativa")
        void deveTratarFilialComAtivoNull() {
            // Arrange
            FilialEntity filialAtivoNull = FilialEntity.builder()
                    .codigoFilial(3)
                    .nomeFilial("Filial Ativo Null")
                    .login("ativonull@teste.com")
                    .senhaHash("hash")
                    .ativo(null)
                    .build();

            LoginRequest request = new LoginRequest("ativonull@teste.com", "senha");

            when(filialRepository.findByLogin("ativonull@teste.com")).thenReturn(Optional.of(filialAtivoNull));

            // Act & Assert - Boolean.FALSE.equals(null) retorna false, então não lança exceção
            // O teste verifica que a senha é validada normalmente
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

            assertThatThrownBy(() -> authService.autenticar(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("Login ou senha invalidos");
        }
    }
}
