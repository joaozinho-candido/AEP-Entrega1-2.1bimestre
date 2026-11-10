package br.com.fomezero.dao;

import br.com.fomezero.conexao.Conexao;
import br.com.fomezero.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UsuarioDAO {

    public void inserir(Usuario u) throws SQLException {
        String sql = "INSERT INTO tb_usuario (nome, email, telefone, tipo_usuario, cpf_cnpj, endereco_horta, "
                + "horas_disponiveis, especialidade, nis, qtd_membros_familia) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, u);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setIdUsuario(rs.getInt(1));
            }
        }
    }

    public void atualizar(Usuario u) throws SQLException {
        String sql = "UPDATE tb_usuario SET nome=?, email=?, telefone=?, tipo_usuario=?, cpf_cnpj=?, endereco_horta=?, "
                + "horas_disponiveis=?, especialidade=?, nis=?, qtd_membros_familia=? WHERE id_usuario=?";
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            preencher(ps, u);
            ps.setInt(11, u.getIdUsuario());
            ps.executeUpdate();
        }
    }

    public boolean excluir(int idUsuario) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("DELETE FROM tb_usuario WHERE id_usuario=?")) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    public Usuario buscarPorId(int idUsuario) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM tb_usuario WHERE id_usuario=?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM tb_usuario WHERE email=?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM tb_usuario ORDER BY id_usuario");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    
    private void preencher(PreparedStatement ps, Usuario u) throws SQLException {
        ps.setString(1, u.getNome());
        ps.setString(2, u.getEmail());
        ps.setString(3, u.getTelefone());
        ps.setString(4, u.getTipoUsuario().name());
        for (int i = 5; i <= 10; i++) ps.setNull(i, Types.NULL);

        if (u instanceof Produtor p) {
            ps.setString(5, p.getCpfCnpj());
            ps.setString(6, p.getEnderecoHorta());
        } else if (u instanceof Voluntario v) {
            ps.setInt(7, v.getHorasDisponiveis());
            ps.setString(8, v.getEspecialidade());
        } else if (u instanceof Beneficiario b) {
            ps.setString(9, b.getNis());
            ps.setInt(10, b.getQtdMembrosFamilia());
        }
    }

   
    static Usuario mapear(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_usuario");
        String nome = rs.getString("nome");
        String email = rs.getString("email");
        String telefone = rs.getString("telefone");
        TipoUsuario tipo = TipoUsuario.valueOf(rs.getString("tipo_usuario"));

        return switch (tipo) {
            case PRODUTOR -> new Produtor(id, nome, email, telefone,
                    rs.getString("cpf_cnpj"), rs.getString("endereco_horta"));
            case VOLUNTARIO -> new Voluntario(id, nome, email, telefone,
                    rs.getInt("horas_disponiveis"), rs.getString("especialidade"));
            case BENEFICIARIO -> new Beneficiario(id, nome, email, telefone,
                    rs.getString("nis"), rs.getInt("qtd_membros_familia"));
        };
    }
}
