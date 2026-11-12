package br.com.fomezero.dao;

import br.com.fomezero.conexao.Conexao;
import br.com.fomezero.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ReservaDAO {

    /** Grava a reserva + itens e desconta o saldo de cada safra (RF03). */
    public void inserir(Reserva r) throws SQLException {
        if (r.getItens().isEmpty()) {
            throw new IllegalArgumentException("A reserva precisa ter ao menos um item.");
        }
        String sqlReserva = "INSERT INTO tb_reserva (id_beneficiario, data_reserva, status) VALUES (?,?,?)";
        String sqlItem = "INSERT INTO tb_item_reserva (id_reserva, id_safra, quantidade_reservada_kg) VALUES (?,?,?)";
        // só desconta se houver saldo suficiente e a safra não estiver vencida
        String sqlBaixa = "UPDATE tb_safra SET quantidade_kg = quantidade_kg - ? "
                + "WHERE id_safra = ? AND quantidade_kg >= ? AND data_validade >= CURDATE()";

        try (Connection con = Conexao.getConexao()) {
            con.setAutoCommit(false);
            try (PreparedStatement psR = con.prepareStatement(sqlReserva, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psI = con.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psB = con.prepareStatement(sqlBaixa)) {

                psR.setInt(1, r.getBeneficiario().getIdUsuario());
                psR.setTimestamp(2, Timestamp.valueOf(r.getDataReserva()));
                psR.setString(3, r.getStatus());
                psR.executeUpdate();
                try (ResultSet rs = psR.getGeneratedKeys()) {
                    if (rs.next()) r.setIdReserva(rs.getInt(1));
                }

                for (ItemReserva item : r.getItens()) {
                    double qtd = item.getQuantidadeReservadaKg();
                    Safra safra = item.getSafra();

                    psB.setDouble(1, qtd);
                    psB.setInt(2, safra.getIdSafra());
                    psB.setDouble(3, qtd);
                    if (psB.executeUpdate() == 0) {
                        throw new SQLException("Saldo insuficiente ou safra vencida: " + safra.getNomeAlimento());
                    }

                    psI.setInt(1, r.getIdReserva());
                    psI.setInt(2, safra.getIdSafra());
                    psI.setDouble(3, qtd);
                    psI.executeUpdate();
                    try (ResultSet rs = psI.getGeneratedKeys()) {
                        if (rs.next()) item.setIdItem(rs.getInt(1));
                    }
                    safra.atualizarEstoque(-qtd); // mantém o objeto em memória sincronizado
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                r.setIdReserva(0);
                throw e;
            }
        }
    }

    public void atualizarStatus(int idReserva, String status) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("UPDATE tb_reserva SET status=? WHERE id_reserva=?")) {
            ps.setString(1, status);
            ps.setInt(2, idReserva);
            ps.executeUpdate();
        }
    }

  
    public boolean cancelar(int idReserva, int idBeneficiario) throws SQLException {
        String sqlStatus = "UPDATE tb_reserva SET status='" + Reserva.CANCELADA + "' "
                + "WHERE id_reserva=? AND id_beneficiario=? AND status='" + Reserva.PENDENTE + "'";
        String sqlDevolve = "UPDATE tb_safra s JOIN tb_item_reserva i ON i.id_safra = s.id_safra "
                + "SET s.quantidade_kg = s.quantidade_kg + i.quantidade_reservada_kg WHERE i.id_reserva=?";
        try (Connection con = Conexao.getConexao()) {
            con.setAutoCommit(false);
            try (PreparedStatement psS = con.prepareStatement(sqlStatus);
                 PreparedStatement psD = con.prepareStatement(sqlDevolve)) {
                psS.setInt(1, idReserva);
                psS.setInt(2, idBeneficiario);
                if (psS.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
                psD.setInt(1, idReserva);
                psD.executeUpdate();
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Remove a reserva  */
    public boolean excluir(int idReserva) throws SQLException {
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement("DELETE FROM tb_reserva WHERE id_reserva=?")) {
            ps.setInt(1, idReserva);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Reserva> listarPorBeneficiario(Beneficiario beneficiario) throws SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM tb_reserva WHERE id_beneficiario=? ORDER BY data_reserva DESC";
        try (Connection con = Conexao.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, beneficiario.getIdUsuario());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reserva r = new Reserva(
                            rs.getInt("id_reserva"),
                            beneficiario,
                            rs.getTimestamp("data_reserva").toLocalDateTime(),
                            rs.getString("status"));
                    carregarItens(con, r);
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    private void carregarItens(Connection con, Reserva r) throws SQLException {
        String sql = "SELECT i.id_item, i.quantidade_reservada_kg, s.*, u.* FROM tb_item_reserva i "
                + "JOIN tb_safra s ON s.id_safra = i.id_safra "
                + "JOIN tb_usuario u ON u.id_usuario = s.id_produtor WHERE i.id_reserva=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getIdReserva());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    r.adicionarItem(new ItemReserva(
                            rs.getInt("id_item"),
                            SafraDAO.mapear(rs),
                            rs.getDouble("quantidade_reservada_kg")));
                }
            }
        }
    }
}
