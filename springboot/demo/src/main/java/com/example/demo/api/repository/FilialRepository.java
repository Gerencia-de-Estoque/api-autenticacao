package com.example.demo.api.repository;

import com.example.demo.api.model.FilialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FilialRepository extends JpaRepository<FilialEntity, Integer> {

    Optional<FilialEntity> findByLogin(String login);
}
