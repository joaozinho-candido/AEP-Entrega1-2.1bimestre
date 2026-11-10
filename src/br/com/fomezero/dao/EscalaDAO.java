package br.com.fomezero.dao;

import br.com.fomezero.conexao.Conexao;
import br.com.fomezero.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EscalaDAO {

    private static final String SELECT_BASE =
            "SELECT e.id_escala, e.tarefa, t.*, u.* FROM tb_escala e "
            + "JOIN tb_turno t ON t.id_turno = e.id_turno "
            + "JOIN tb_usuario u ON u.id_usuario = e.id_voluntario ";

    /** Escala um voluntário. Valida que o usuário é VOLUNTARIO e que não há choque de horário. */
    public void inserir(Escala e) throws SQLException {
        if (e.getVoluntario().getTipoUsuario() != TipoUsuario.VOLUNTARIO) {
            throw new IllegalArgumentException("Somente voluntários podem ser escalados.");
        }
        if (possuiConflitoDeHorario(e.getVoluntario().getIdUsuario(), e.getTurno())) {
            throw new IllegalArgumentException(e.getVoluntario().getNome()
                    + " já está escalado(a) em um turno nesse horário.");
        }
        String sql = "INSERT INTO tb_escala (id_turno, id_voluntario, tarefa) VALUES (?,?,?)";
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getTurno().getIdTurno());
            ps.setInt(2, e.getVoluntario().getIdUsuario());
            ps.setString(3, e.getTarefa().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) e.setIdEscala(rs.getInt(1));
            }
        }
        e.getTurno().adicionarEscala(e);
        e.getVoluntario().adicionarEscala(e);
    }

    public void atualizarTarefa(int idEscala, Tarefa tarefa) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("UPDATE tb_escala SET tarefa=? WHERE id_escala=?")) {
            ps.setString(1, tarefa.name());
            ps.setInt(2, idEscala);
            ps.executeUpdate();
        }
    }

    public boolean excluir(int idEscala) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("DELETE FROM tb_escala WHERE id_escala=?")) {
            ps.setInt(1, idEscala);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Escala> listarPorTurno(int idTurno) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "WHERE e.id_turno=? ORDER BY u.nome")) {
            ps.setInt(1, idTurno);
            return listar(ps);
        }
    }

    /** Carrega as escalas do voluntário e também as vincula ao objeto. */
    public List<Escala> listarPorVoluntario(Voluntario v) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE
                     + "WHERE e.id_voluntario=? ORDER BY t.data_turno, t.hora_inicio")) {
            ps.setInt(1, v.getIdUsuario());
            List<Escala> lista = listar(ps);
            v.getEscalas().clear();
            lista.forEach(v::adicionarEscala);
            return lista;
        }
    }

    /** Relatório de carga horária de todos os voluntários. */
    public List<String> relatorioCargaHoraria() throws SQLException {
        List<String> linhas = new ArrayList<>();
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM vw_carga_horaria_voluntario ORDER BY total_horas DESC, nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                linhas.add(String.format("[%d] %-25s %3d turno(s) | %6.1f h",
                        rs.getInt("id_voluntario"), rs.getString("nome"),
                        rs.getInt("qtd_turnos"), rs.getDouble("total_horas")));
            }
        }
        return linhas;
    }

    private boolean possuiConflitoDeHorario(int idVoluntario, Turno turno) throws SQLException {
        String sql = "SELECT 1 FROM tb_escala e JOIN tb_turno t ON t.id_turno = e.id_turno "
                + "WHERE e.id_voluntario=? AND t.data_turno=? AND t.hora_inicio < ? AND t.hora_fim > ?";
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVoluntario);
            ps.setDate(2, Date.valueOf(turno.getDataTurno()));
            ps.setTime(3, Time.valueOf(turno.getHoraFim()));
            ps.setTime(4, Time.valueOf(turno.getHoraInicio()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private List<Escala> listar(PreparedStatement ps) throws SQLException {
        List<Escala> lista = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Escala(
                        rs.getInt("id_escala"),
                        TurnoDAO.mapear(rs),
                        (Voluntario) UsuarioDAO.mapear(rs),
                        Tarefa.valueOf(rs.getString("tarefa"))));
            }
        }
        return lista;
    }
}
