package br.com.fomezero.dao;

import br.com.fomezero.conexao.Conexao;
import br.com.fomezero.model.Turno;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class TurnoDAO {

    public void inserir(Turno t) throws SQLException {
        String sql = "INSERT INTO tb_turno (data_turno, hora_inicio, hora_fim, descricao) VALUES (?,?,?,?)";
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, t);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) t.setIdTurno(rs.getInt(1));
            }
        }
    }

    public void atualizar(Turno t) throws SQLException {
        String sql = "UPDATE tb_turno SET data_turno=?, hora_inicio=?, hora_fim=?, descricao=? WHERE id_turno=?";
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            preencher(ps, t);
            ps.setInt(5, t.getIdTurno());
            ps.executeUpdate();
        }
    }


    public boolean excluir(int idTurno) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("DELETE FROM tb_turno WHERE id_turno=?")) {
            ps.setInt(1, idTurno);
            return ps.executeUpdate() > 0;
        }
    }

    public Turno buscarPorId(int idTurno) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM tb_turno WHERE id_turno=?")) {
            ps.setInt(1, idTurno);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Turno> listar() throws SQLException {
        List<Turno> lista = new ArrayList<>();
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM tb_turno ORDER BY data_turno, hora_inicio");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private void preencher(PreparedStatement ps, Turno t) throws SQLException {
        ps.setDate(1, Date.valueOf(t.getDataTurno()));
        ps.setTime(2, Time.valueOf(t.getHoraInicio()));
        ps.setTime(3, Time.valueOf(t.getHoraFim()));
        ps.setString(4, t.getDescricao());
    }

    static Turno mapear(ResultSet rs) throws SQLException {
        return new Turno(
                rs.getInt("id_turno"),
                rs.getDate("data_turno").toLocalDate(),
                rs.getTime("hora_inicio").toLocalTime(),
                rs.getTime("hora_fim").toLocalTime(),
                rs.getString("descricao"));
    }
}
