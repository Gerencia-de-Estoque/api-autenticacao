CREATE TABLE IF NOT EXISTS tb_filial (
    codigo_filial INT AUTO_INCREMENT PRIMARY KEY,
    nome_filial VARCHAR(150) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tb_material_construcao (
    codigo_material INT AUTO_INCREMENT PRIMARY KEY,
    codigo_produto VARCHAR(50) NOT NULL,
    valor DOUBLE NOT NULL,
    cor VARCHAR(50),
    nome VARCHAR(150) NOT NULL,
    materia_prima VARCHAR(100),
    codigo_filial INT NOT NULL,
    CONSTRAINT fk_material_filial FOREIGN KEY (codigo_filial)
        REFERENCES tb_filial (codigo_filial)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tb_ferramenta (
    codigo_ferramenta INT AUTO_INCREMENT PRIMARY KEY,
    codigo_produto VARCHAR(50) NOT NULL,
    valor DOUBLE NOT NULL,
    marca VARCHAR(100),
    nome VARCHAR(150) NOT NULL,
    qtd_pacote INT,
    codigo_filial INT NOT NULL,
    CONSTRAINT fk_ferramenta_filial FOREIGN KEY (codigo_filial)
        REFERENCES tb_filial (codigo_filial)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
