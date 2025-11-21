CREATE TABLE tb_filial (
    codigo_filial INT AUTO_INCREMENT PRIMARY KEY,
    nome_filial VARCHAR(150) NOT NULL,
    login VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    ativo BOOLEAN NOT NULL
);
