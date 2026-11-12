package br.com.fomezero.model;

import java.util.ArrayList;
import java.util.List;

public class Voluntario extends Usuario {

    private int horasDisponiveis;
    private String especialidade;
    private final List<Escala> escalas = new ArrayList<>(); // 1 Voluntario -> N Escalas (RF04)

    public Voluntario(int idUsuario, String nome, String email, String telefone,
                      int horasDisponiveis, String especialidade) {
        super(idUsuario, nome, email, telefone);
        this.horasDisponiveis = horasDisponiveis;
        this.especialidade = especialidade;
    }

    @Override
    public void exibirMenu() {
        System.out.println("\n===== MENU VOLUNTÁRIO - " + getNome() + " =====");
        System.out.println("1 - Consultar estoque de insumos");
        System.out.println("2 - Cadastrar novo insumo");
        System.out.println("3 - Registrar entrada de insumo");
        System.out.println("4 - Dar baixa em insumo");
        System.out.println("5 - Ver meu perfil");
        System.out.println("6 - Minhas escalas e carga horária");
        System.out.println("0 - Sair");
    }

    @Override
    public TipoUsuario getTipoUsuario() { return TipoUsuario.VOLUNTARIO; }

    public void adicionarEscala(Escala escala) { escalas.add(escala); }
    public List<Escala> getEscalas() { return escalas; }

   
    public double calcularCargaHoraria() {
        return escalas.stream().mapToDouble(e -> e.getTurno().calcularDuracaoHoras()).sum();
    }

    public int getHorasDisponiveis() { return horasDisponiveis; }
    public void setHorasDisponiveis(int horasDisponiveis) { this.horasDisponiveis = horasDisponiveis; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    @Override
    public String toString() {
        return super.toString() + " | Horas disp.: " + horasDisponiveis + " | Especialidade: " + especialidade;
    }
}
