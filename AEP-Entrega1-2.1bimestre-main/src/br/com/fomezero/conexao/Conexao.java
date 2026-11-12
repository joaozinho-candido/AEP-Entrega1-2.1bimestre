package br.com.fomezero.conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public final class Conexao {

    private static final String URL = env("FOMEZERO_DB_URL",
            "jdbc:mysql://localhost:3306/fome_zero?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true");
    private static final String USUARIO = env("FOMEZERO_DB_USER", "root");
    private static final String SENHA = env("FOMEZERO_DB_PASS", "Joaocan090107@");

    private Conexao() { }

    public static Connection getConexao() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    private static String env(String nome, String padrao) {
        String valor = System.getenv(nome);
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }
}
