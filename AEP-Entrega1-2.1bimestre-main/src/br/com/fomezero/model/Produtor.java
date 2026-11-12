package br.com.fomezero.model;

import java.util.ArrayList;
import java.util.List;

public class Produtor extends Usuario {

    private String cpfCnpj;
    private String enderecoHorta;
    private final List<Safra> safras = new ArrayList<>(); // 1 Produtor -> N Safras

    public Produtor(int idUsuario, String nome, String email, String telefone,
                    String cpfCnpj, String enderecoHorta) {
        super(idUsuario, nome, email, telefone);
        this.cpfCnpj = cpfCnpj;
        this.enderecoHorta = enderecoHorta;
    }

    @Override
    public void exibirMenu() {
        System.out.println("\n===== MENU PRODUTOR/DOADOR - " + getNome() + " =====");
        System.out.println("1 - Cadastrar nova safra");
        System.out.println("2 - Listar minhas safras");
        System.out.println("3 - Atualizar safra");
        System.out.println("4 - Excluir safra");
        System.out.println("0 - Sair");
    }

    @Override
    public TipoUsuario getTipoUsuario() { return TipoUsuario.PRODUTOR; }

    public void adicionarSafra(Safra safra) { safras.add(safra); }
    public List<Safra> getSafras() { return safras; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getEnderecoHorta() { return enderecoHorta; }
    public void setEnderecoHorta(String enderecoHorta) { this.enderecoHorta = enderecoHorta; }

    @Override
    public String toString() {
        return super.toString() + " | CPF/CNPJ: " + cpfCnpj + " | Horta: " + enderecoHorta;
    }
}
