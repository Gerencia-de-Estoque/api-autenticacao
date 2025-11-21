package com.example.demo.api.model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "tb_filial")
public class FilialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "codigo_filial",
            nullable = false,
            updatable = false,
            columnDefinition = "INT AUTO_INCREMENT"
    )
    private Integer codigoFilial;

    @Column(name = "nome_filial", length = 150, nullable = false)
    private String nomeFilial;

    // --- AUTENTICAÇÃO AQUI ---
    @Column(
            name = "login",
            length = 100,
            nullable = false,
            unique = true
    )
    private String login;

    @Column(
            name = "senha_hash",
            length = 255,
            nullable = false
    )
    private String senhaHash;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    // relacionamento inverso das ferramentas
    // @JsonManagedReference("filial-ferramentas")
    // @OneToMany(mappedBy = "filial", fetch = FetchType.LAZY)
    // private List<FerramentaEntity> ferramentas;
}
