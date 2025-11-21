package com.example.demo.api.security;

import com.example.demo.api.model.FilialEntity;
import com.example.demo.api.repository.FilialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilialDetailsService implements UserDetailsService {

    private final FilialRepository filialRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        FilialEntity filial = filialRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Filial nao encontrada para login: " + username));
        return new FilialDetails(filial);
    }
}
