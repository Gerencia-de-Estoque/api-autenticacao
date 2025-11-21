package com.example.demo.api.service;

import com.example.demo.api.dto.LoginRequest;
import com.example.demo.api.dto.LoginResponse;
import com.example.demo.api.model.FilialEntity;
import com.example.demo.api.repository.FilialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final FilialRepository filialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse autenticar(LoginRequest request) {
        FilialEntity filial = filialRepository.findByLogin(request.login())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login ou senha invalidos"));

        if (Boolean.FALSE.equals(filial.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Filial desativada");
        }

        if (!passwordEncoder.matches(request.senha(), filial.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login ou senha invalidos");
        }

        String token = jwtService.generateToken(filial);
        Instant expiraEm = jwtService.extractExpirationInstant(token);

        return new LoginResponse(token, "Bearer", expiraEm);
    }
}
