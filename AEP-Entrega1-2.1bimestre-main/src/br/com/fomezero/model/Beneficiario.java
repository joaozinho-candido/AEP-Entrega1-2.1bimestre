package br.com.fomezero.model;

import java.util.ArrayList;
import java.util.List;

public class Beneficiario extends Usuario {

    private String nis;
    private int qtdMembrosFamilia;
    private final List<Reserva> reservas = new ArrayList<>(); // 1 Beneficiario -> N Reservas

    public Beneficiario(int idUsuario, String nome, String email, String telefone,
                        String nis, int qtdMembrosFamilia) {
        super(idUsuario, nome, email, telefone);
        this.nis = nis;
        this.qtdMembrosFamilia = qtdMembrosFamilia;
    }

    @Override
    public void exibirMenu() {
        System.out.println("\n===== MENU BENEFICIÁRIO - " + getNome() + " =====");
        System.out.println("1 - Consultar alimentos disponíveis");
        System.out.println("2 - Fazer reserva de cesta");
        System.out.println("3 - Minhas reservas");
        System.out.println("4 - Cancelar reserva");
        System.out.println("0 - Sair");
    }

    @Override
    public TipoUsuario getTipoUsuario() { return TipoUsuario.BENEFICIARIO; }

    public void adicionarReserva(Reserva reserva) { reservas.add(reserva); }
    public List<Reserva> getReservas() { return reservas; }

    public String getNis() { return nis; }
    public void setNis(String nis) { this.nis = nis; }

    public int getQtdMembrosFamilia() { return qtdMembrosFamilia; }
    public void setQtdMembrosFamilia(int qtdMembrosFamilia) { this.qtdMembrosFamilia = qtdMembrosFamilia; }

    @Override
    public String toString() {
        return super.toString() + " | NIS: " + nis + " | Membros da família: " + qtdMembrosFamilia;
    }
}
