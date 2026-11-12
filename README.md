# Projeto Fome Zero

Trabalho da AEP - Unicesumar
Alunos: João Cândido Scaramal Scolari (RA 26007770-2) e Andrey Bras Bissoli (RA 25201495-2)

Sistema em Java com banco MySQL para gerenciar hortas comunitárias e doações de alimentos (ODS 2).

## O que precisa ter instalado

- Java 17 ou mais novo
- MySQL 8
- Uma IDE Java (usamos o VS Code, mas IntelliJ ou Eclipse também funcionam)

## Como rodar

1. Baixe o projeto (Code > Download ZIP) e extraia.

2. Rode o script sql/fome_zero.sql no MySQL. Ele cria o banco, as tabelas e alguns dados de teste.
   No MySQL Workbench é só abrir o arquivo e executar.
   No MySQL Command Line Client dá pra usar:
   SOURCE C:/caminho/da/pasta/sql/fome_zero.sql

3. Abra o arquivo src/br/com/fomezero/conexao/Conexao.java e coloque a senha do seu MySQL na linha da SENHA (no lugar de "root").

4. Abra a pasta do projeto na IDE. Como é um projeto Maven, o driver do MySQL é baixado sozinho.

5. Rode a classe Main, que fica em src/br/com/fomezero/view.

## Testando

Para ver se conectou no banco, escolha a opção 3 do menu (Listar usuários). Devem aparecer 3 usuários.

Para entrar no sistema, use a opção 2 com um desses e-mails:

- horta@alvorada.org (produtor) - cadastro de safras
- maria@email.com (voluntário) - estoque de insumos e horas trabalhadas
- jose@email.com (beneficiário) - reserva de cestas

A opção 5 do menu é a parte da administração, onde são criados os turnos e a escala dos voluntários.

## Se der erro

- "Access denied": a senha no Conexao.java está errada.
- "Communications link failure": o MySQL não está ligado.
- "Unknown database": o script do passo 2 não foi executado.
