
SET NAMES utf8mb4;

DROP DATABASE IF EXISTS fome_zero;
CREATE DATABASE fome_zero
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE fome_zero;

-- ---------------------------------------------------------------------
-- Tabela tb_usuario
-- Tabela única para a hierarquia Usuario -> Produtor/Voluntario/Beneficiario.
-- A coluna discriminadora tipo_usuario define o perfil; os campos
-- específicos de cada perfil ficam NULL para os demais.
-- ---------------------------------------------------------------------
CREATE TABLE tb_usuario (
    id_usuario          INT AUTO_INCREMENT PRIMARY KEY,
    nome                VARCHAR(100) NOT NULL,
    email               VARCHAR(100) NOT NULL UNIQUE,
    telefone            VARCHAR(20),
    tipo_usuario        ENUM('PRODUTOR', 'VOLUNTARIO', 'BENEFICIARIO') NOT NULL,
    -- Produtor
    cpf_cnpj            VARCHAR(18),
    endereco_horta      VARCHAR(200),
    -- Voluntario
    horas_disponiveis   INT,
    especialidade       VARCHAR(100),
    -- Beneficiario
    nis                 VARCHAR(20),
    qtd_membros_familia INT,
    CONSTRAINT ck_horas_disponiveis CHECK (horas_disponiveis IS NULL OR horas_disponiveis >= 0),
    CONSTRAINT ck_qtd_membros       CHECK (qtd_membros_familia IS NULL OR qtd_membros_familia > 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Tabela tb_safra  (Usuario/Produtor 1 ---- 0..N Safra)
-- ---------------------------------------------------------------------
CREATE TABLE tb_safra (
    id_safra        INT AUTO_INCREMENT PRIMARY KEY,
    id_produtor     INT NOT NULL,
    nome_alimento   VARCHAR(100) NOT NULL,
    quantidade_kg   DOUBLE NOT NULL,
    data_colheita   DATE NOT NULL,
    data_validade   DATE NOT NULL,
    CONSTRAINT ck_safra_quantidade CHECK (quantidade_kg >= 0),
    CONSTRAINT ck_safra_datas      CHECK (data_validade >= data_colheita),
    CONSTRAINT fk_safra_produtor FOREIGN KEY (id_produtor)
        REFERENCES tb_usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Tabela tb_reserva  (Usuario/Beneficiario 1 ---- 0..N Reserva)
-- ---------------------------------------------------------------------
CREATE TABLE tb_reserva (
    id_reserva      INT AUTO_INCREMENT PRIMARY KEY,
    id_beneficiario INT NOT NULL,
    data_reserva    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status          VARCHAR(20) NOT NULL DEFAULT 'Pendente',
    CONSTRAINT ck_reserva_status CHECK (status IN ('Pendente', 'Concluida', 'Cancelada')),
    CONSTRAINT fk_reserva_beneficiario FOREIGN KEY (id_beneficiario)
        REFERENCES tb_usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Tabela tb_item_reserva 
-- Reserva 1 ---- 1..N Item_Reserva ; Safra 1 ---- 0..N Item_Reserva
-- ---------------------------------------------------------------------
CREATE TABLE tb_item_reserva (
    id_item                 INT AUTO_INCREMENT PRIMARY KEY,
    id_reserva              INT NOT NULL,
    id_safra                INT NOT NULL,
    quantidade_reservada_kg DOUBLE NOT NULL,
    CONSTRAINT ck_item_quantidade CHECK (quantidade_reservada_kg > 0),
    CONSTRAINT fk_item_reserva FOREIGN KEY (id_reserva)
        REFERENCES tb_reserva (id_reserva)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_item_safra FOREIGN KEY (id_safra)
        REFERENCES tb_safra (id_safra)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Tabela tb_insumo (independente)
-- ---------------------------------------------------------------------
CREATE TABLE tb_insumo (
    id_insumo           INT AUTO_INCREMENT PRIMARY KEY,
    nome_insumo         VARCHAR(100) NOT NULL,
    quantidade_estoque  INT NOT NULL DEFAULT 0,
    CONSTRAINT ck_insumo_estoque CHECK (quantidade_estoque >= 0)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Tabela tb_turno  - turnos de trabalho criados pela administração
-- ---------------------------------------------------------------------
CREATE TABLE tb_turno (
    id_turno    INT AUTO_INCREMENT PRIMARY KEY,
    data_turno  DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim    TIME NOT NULL,
    descricao   VARCHAR(150),
    CONSTRAINT ck_turno_horario CHECK (hora_fim > hora_inicio)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- Tabela tb_escala 
-- indicando a tarefa atribuída ao voluntário naquele turno.
-- ---------------------------------------------------------------------
CREATE TABLE tb_escala (
    id_escala     INT AUTO_INCREMENT PRIMARY KEY,
    id_turno      INT NOT NULL,
    id_voluntario INT NOT NULL,
    tarefa        ENUM('PLANTIO', 'REGA', 'COLHEITA', 'MONTAGEM_CESTAS') NOT NULL,
    CONSTRAINT uq_escala_turno_voluntario UNIQUE (id_turno, id_voluntario),
    CONSTRAINT fk_escala_turno FOREIGN KEY (id_turno)
        REFERENCES tb_turno (id_turno)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_escala_voluntario FOREIGN KEY (id_voluntario)
        REFERENCES tb_usuario (id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB;

-- View com a carga horária dedicada por voluntário (RF04)
CREATE VIEW vw_carga_horaria_voluntario AS
SELECT u.id_usuario AS id_voluntario,
       u.nome,
       COUNT(e.id_escala) AS qtd_turnos,
       COALESCE(SUM(TIME_TO_SEC(TIMEDIFF(t.hora_fim, t.hora_inicio))) / 3600, 0) AS total_horas
FROM tb_usuario u
LEFT JOIN tb_escala e ON e.id_voluntario = u.id_usuario
LEFT JOIN tb_turno  t ON t.id_turno = e.id_turno
WHERE u.tipo_usuario = 'VOLUNTARIO'
GROUP BY u.id_usuario, u.nome;

-- =====================================================================
-- DADOS DE EXEMPLO (opcional)
-- =====================================================================
INSERT INTO tb_usuario (nome, email, telefone, tipo_usuario, cpf_cnpj, endereco_horta)
VALUES ('Horta Comunitária Jardim Alvorada', 'horta@alvorada.org', '(44) 99999-0001',
        'PRODUTOR', '12.345.678/0001-90', 'Rua das Hortas, 100 - Maringá/PR');

INSERT INTO tb_usuario (nome, email, telefone, tipo_usuario, horas_disponiveis, especialidade)
VALUES ('Maria Souza', 'maria@email.com', '(44) 99999-0002', 'VOLUNTARIO', 12, 'Plantio');

INSERT INTO tb_usuario (nome, email, telefone, tipo_usuario, nis, qtd_membros_familia)
VALUES ('José da Silva', 'jose@email.com', '(44) 99999-0003', 'BENEFICIARIO', '12345678901', 4);

INSERT INTO tb_safra (id_produtor, nome_alimento, quantidade_kg, data_colheita, data_validade) VALUES
    (1, 'Alface',  30.0, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
    (1, 'Tomate',  80.5, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 10 DAY)),
    (1, 'Cenoura', 55.0, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 20 DAY));

INSERT INTO tb_insumo (nome_insumo, quantidade_estoque) VALUES
    ('Pacote de sementes de alface', 50),
    ('Adubo orgânico (saco 20kg)', 10),
    ('Mudas de tomate', 120),
    ('Enxada', 8);

INSERT INTO tb_turno (data_turno, hora_inicio, hora_fim, descricao) VALUES
    (DATE_ADD(CURDATE(), INTERVAL 1 DAY), '07:00', '11:00', 'Turno da manhã'),
    (DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00', '17:30', 'Turno da tarde');

INSERT INTO tb_escala (id_turno, id_voluntario, tarefa) VALUES
    (1, 2, 'PLANTIO'),
    (2, 2, 'MONTAGEM_CESTAS');
