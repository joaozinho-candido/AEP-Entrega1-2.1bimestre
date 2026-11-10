package br.com.fomezero.dao;

import br.com.fomezero.conexao.Conexao;
import br.com.fomezero.model.Insumo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class InsumoDAO {

    public void inserir(Insumo i) throws SQLException {
        String sql = "INSERT INTO tb_insumo (nome_insumo, quantidade_estoque) VALUES (?,?)";
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, i.getNomeInsumo());
            ps.setInt(2, i.getQuantidadeEstoque());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) i.setIdInsumo(rs.getInt(1));
            }
        }
    }

    /** Persiste o estado do objeto (usado após adicionarEstoque / darBaixaEstoque). */
    public void atualizar(Insumo i) throws SQLException {
        String sql = "UPDATE tb_insumo SET nome_insumo=?, quantidade_estoque=? WHERE id_insumo=?";
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, i.getNomeInsumo());
            ps.setInt(2, i.getQuantidadeEstoque());
            ps.setInt(3, i.getIdInsumo());
            ps.executeUpdate();
        }
    }

    public boolean excluir(int idInsumo) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("DELETE FROM tb_insumo WHERE id_insumo=?")) {
            ps.setInt(1, idInsumo);
            return ps.executeUpdate() > 0;
        }
    }

    public Insumo buscarPorId(int idInsumo) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM tb_insumo WHERE id_insumo=?")) {
            ps.setInt(1, idInsumo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Insumo> listar() throws SQLException {
        List<Insumo> lista = new ArrayList<>();
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM tb_insumo ORDER BY nome_insumo");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private Insumo mapear(ResultSet rs) throws SQLException {
        return new Insumo(rs.getInt("id_insumo"), rs.getString("nome_insumo"), rs.getInt("quantidade_estoque"));
    }
}
