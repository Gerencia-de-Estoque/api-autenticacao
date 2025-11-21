package com.example.demo.api.security;

import com.example.demo.api.model.FilialEntity;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@SuppressFBWarnings({"SE_BAD_FIELD", "SE_NO_SERIALVERSIONID", "EI_EXPOSE_REP2"})
public class FilialDetails implements UserDetails {

    private final FilialEntity filial;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_FILIAL"));
    }

    @Override
    public String getPassword() {
        return filial.getSenhaHash();
    }

    @Override
    public String getUsername() {
        return filial.getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(filial.getAtivo());
    }
}
