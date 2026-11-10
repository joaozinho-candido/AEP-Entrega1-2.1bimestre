package br.com.fomezero.dao;

import br.com.fomezero.conexao.Conexao;
import br.com.fomezero.model.Produtor;
import br.com.fomezero.model.Safra;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class SafraDAO {

    private static final String SELECT_BASE =
            "SELECT s.*, u.* FROM tb_safra s JOIN tb_usuario u ON u.id_usuario = s.id_produtor ";

    public void inserir(Safra s) throws SQLException {
        String sql = "INSERT INTO tb_safra (id_produtor, nome_alimento, quantidade_kg, data_colheita, data_validade) "
                + "VALUES (?,?,?,?,?)";
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getProdutor().getIdUsuario());
            ps.setString(2, s.getNomeAlimento());
            ps.setDouble(3, s.getQuantidadeKg());
            ps.setDate(4, Date.valueOf(s.getDataColheita()));
            ps.setDate(5, Date.valueOf(s.getDataValidade()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.setIdSafra(rs.getInt(1));
            }
        }
    }

    public void atualizar(Safra s) throws SQLException {
        String sql = "UPDATE tb_safra SET nome_alimento=?, quantidade_kg=?, data_colheita=?, data_validade=? "
                + "WHERE id_safra=?";
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getNomeAlimento());
            ps.setDouble(2, s.getQuantidadeKg());
            ps.setDate(3, Date.valueOf(s.getDataColheita()));
            ps.setDate(4, Date.valueOf(s.getDataValidade()));
            ps.setInt(5, s.getIdSafra());
            ps.executeUpdate();
        }
    }

    public boolean excluir(int idSafra) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("DELETE FROM tb_safra WHERE id_safra=?")) {
            ps.setInt(1, idSafra);
            return ps.executeUpdate() > 0;
        }
    }

    public Safra buscarPorId(int idSafra) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "WHERE s.id_safra=?")) {
            ps.setInt(1, idSafra);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Safra> listarPorProdutor(int idProdutor) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "WHERE s.id_produtor=? ORDER BY s.data_validade")) {
            ps.setInt(1, idProdutor);
            return listar(ps);
        }
    }

    /** Lotes com saldo > 0 e dentro da validade (consulta do beneficiário - RF03). */
    public List<Safra> listarDisponiveis() throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE
                     + "WHERE s.quantidade_kg > 0 AND s.data_validade >= CURDATE() ORDER BY s.data_validade")) {
            return listar(ps);
        }
    }

    private List<Safra> listar(PreparedStatement ps) throws SQLException {
        List<Safra> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    static Safra mapear(ResultSet rs) throws SQLException {
        Produtor produtor = (Produtor) UsuarioDAO.mapear(rs);
        return new Safra(
                rs.getInt("id_safra"),
                produtor,
                rs.getString("nome_alimento"),
                rs.getDouble("quantidade_kg"),
                rs.getDate("data_colheita").toLocalDate(),
                rs.getDate("data_validade").toLocalDate());
    }
}
