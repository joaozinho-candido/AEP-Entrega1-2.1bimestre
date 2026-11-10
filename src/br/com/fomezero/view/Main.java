package br.com.fomezero.view;

import br.com.fomezero.dao.EscalaDAO;
import br.com.fomezero.dao.InsumoDAO;
import br.com.fomezero.dao.ReservaDAO;
import br.com.fomezero.dao.SafraDAO;
import br.com.fomezero.dao.TurnoDAO;
import br.com.fomezero.dao.UsuarioDAO;
import br.com.fomezero.model.*;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;


public class Main {

    private static final Scanner sc = new Scanner(System.in);
    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final SafraDAO safraDAO = new SafraDAO();
    private static final ReservaDAO reservaDAO = new ReservaDAO();
    private static final InsumoDAO insumoDAO = new InsumoDAO();
    private static final TurnoDAO turnoDAO = new TurnoDAO();
    private static final EscalaDAO escalaDAO = new EscalaDAO();

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   PROJETO FOME ZERO - ODS 2 (Unicesumar)        ");
        System.out.println("=================================================");

        int opcao;
        do {
            System.out.println("\n===== MENU PRINCIPAL =====");
            System.out.println("1 - Cadastrar usuário");
            System.out.println("2 - Entrar (por e-mail)");
            System.out.println("3 - Listar usuários");
            System.out.println("4 - Excluir usuário");
            System.out.println("5 - Administração (turnos e escala de voluntários)");
            System.out.println("0 - Encerrar");
            opcao = lerInt("Opção: ");
            try {
                switch (opcao) {
                    case 1 -> cadastrarUsuario();
                    case 2 -> entrar();
                    case 3 -> usuarioDAO.listar().forEach(System.out::println);
                    case 4 -> excluirUsuario();
                    case 5 -> menuAdministracao();
                    case 0 -> System.out.println("Até logo!");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (SQLException e) {
                System.out.println("Erro de banco de dados: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    // ------------------------------------------------------------------ usuários 

    private static void cadastrarUsuario() throws SQLException {
        System.out.println("Perfil: 1 - Produtor/Doador | 2 - Voluntário | 3 - Beneficiário");
        int perfil = lerInt("Perfil: ");
        if (perfil < 1 || perfil > 3) {
            System.out.println("Perfil inválido.");
            return;
        }
        String nome = lerTexto("Nome: ");
        String email = lerTexto("E-mail: ");
        String telefone = lerTexto("Telefone: ");

        Usuario usuario = switch (perfil) {
            case 1 -> new Produtor(0, nome, email, telefone,
                    lerTexto("CPF/CNPJ: "), lerTexto("Endereço da horta: "));
            case 2 -> new Voluntario(0, nome, email, telefone,
                    lerInt("Horas disponíveis por semana: "), lerTexto("Especialidade (plantio, rega, colheita...): "));
            default -> new Beneficiario(0, nome, email, telefone,
                    lerTexto("NIS: "), lerInt("Quantidade de membros da família: "));
        };
        usuarioDAO.inserir(usuario);
        System.out.println("Usuário cadastrado com ID " + usuario.getIdUsuario());
    }

    private static void excluirUsuario() throws SQLException {
        int id = lerInt("ID do usuário: ");
        try {
            System.out.println(usuarioDAO.excluir(id) ? "Usuário excluído." : "Usuário não encontrado.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Não é possível excluir: o usuário possui safras ou reservas vinculadas.");
        }
    }

    private static void entrar() throws SQLException {
        Usuario usuario = usuarioDAO.buscarPorEmail(lerTexto("E-mail: "));
        if (usuario == null) {
            System.out.println("Usuário não encontrado.");
            return;
        }
        int opcao;
        do {
            usuario.exibirMenu(); // POLIMORFISMO: cada subclasse mostra seu próprio menu
            opcao = lerInt("Opção: ");
            if (opcao == 0) break;
            try {
                if (usuario instanceof Produtor p) menuProdutor(p, opcao);
                else if (usuario instanceof Voluntario v) menuVoluntario(v, opcao);
                else if (usuario instanceof Beneficiario b) menuBeneficiario(b, opcao);
            } catch (SQLException e) {
                System.out.println("Erro de banco de dados: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Atenção: " + e.getMessage());
            }
        } while (true);
    }


    // ------------------------------------------------------------------ produtor 

    private static void menuProdutor(Produtor p, int opcao) throws SQLException {
        switch (opcao) {
            case 1 -> {
                Safra s = new Safra(0, p, lerTexto("Alimento: "),
                        lerDouble("Quantidade (kg/unidades): "),
                        lerData("Data de colheita (dd/MM/aaaa): "),
                        lerData("Data limite para consumo/validade (dd/MM/aaaa): "));
                safraDAO.inserir(s);
                p.adicionarSafra(s);
                System.out.println("Safra cadastrada com ID " + s.getIdSafra());
            }
            case 2 -> listar(safraDAO.listarPorProdutor(p.getIdUsuario()), "Nenhuma safra cadastrada.");
            case 3 -> {
                Safra s = buscarSafraDoProdutor(p);
                if (s == null) return;
                String nome = lerTexto("Novo nome (Enter mantém '" + s.getNomeAlimento() + "'): ");
                if (!nome.isBlank()) s.setNomeAlimento(nome);
                double delta = lerDouble("Ajuste de quantidade em kg (+ entrada / - saída, 0 mantém): ");
                s.atualizarEstoque(delta);
                LocalDate colheita = lerDataOpcional("Nova colheita dd/MM/aaaa (Enter mantém "
                        + s.getDataColheita().format(FMT_DATA) + "): ", s.getDataColheita());
                LocalDate validade = lerDataOpcional("Nova validade dd/MM/aaaa (Enter mantém "
                        + s.getDataValidade().format(FMT_DATA) + "): ", s.getDataValidade());
                // recria o objeto para validar colheita e validade em conjunto
                s = new Safra(s.getIdSafra(), p, s.getNomeAlimento(), s.getQuantidadeKg(), colheita, validade);
                safraDAO.atualizar(s);
                System.out.println("Safra atualizada: " + s);
            }
            case 4 -> {
                Safra s = buscarSafraDoProdutor(p);
                if (s == null) return;
                try {
                    safraDAO.excluir(s.getIdSafra());
                    System.out.println("Safra excluída.");
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Não é possível excluir: a safra já possui reservas.");
                }
            }
            default -> System.out.println("Opção inválida.");
        }
    }

    private static Safra buscarSafraDoProdutor(Produtor p) throws SQLException {
        Safra s = safraDAO.buscarPorId(lerInt("ID da safra: "));
        if (s == null || s.getProdutor().getIdUsuario() != p.getIdUsuario()) {
            System.out.println("Safra não encontrada entre as suas.");
            return null;
        }
        return s;
    }

    // ------------------------------------------------------------------ voluntário 

    private static void menuVoluntario(Voluntario v, int opcao) throws SQLException {
        switch (opcao) {
            case 1 -> listar(insumoDAO.listar(), "Nenhum insumo cadastrado.");
            case 2 -> {
                Insumo i = new Insumo(0, lerTexto("Nome do insumo: "), lerInt("Quantidade inicial: "));
                insumoDAO.inserir(i);
                System.out.println("Insumo cadastrado com ID " + i.getIdInsumo());
            }
            case 3, 4 -> {
                Insumo i = insumoDAO.buscarPorId(lerInt("ID do insumo: "));
                if (i == null) {
                    System.out.println("Insumo não encontrado.");
                    return;
                }
                int qtd = lerInt("Quantidade: ");
                if (opcao == 3) i.adicionarEstoque(qtd);
                else i.darBaixaEstoque(qtd);
                insumoDAO.atualizar(i);
                System.out.println("Estoque atualizado: " + i);
            }
            case 5 -> System.out.println(v);
            case 6 -> {
                listar(escalaDAO.listarPorVoluntario(v), "Você ainda não foi escalado(a) em nenhum turno.");
                System.out.printf("Carga horária total dedicada: %.1f h%n", v.calcularCargaHoraria());
            }
            default -> System.out.println("Opção inválida.");
        }
    }

    // ------------------------------------------------------------------ administração 

    private static void menuAdministracao() throws SQLException {
        int opcao;
        do {
            System.out.println("\n===== ADMINISTRAÇÃO - TURNOS E VOLUNTÁRIOS =====");
            System.out.println("1 - Criar turno");
            System.out.println("2 - Listar turnos");
            System.out.println("3 - Excluir turno");
            System.out.println("4 - Escalar voluntário em turno (atribuir tarefa)");
            System.out.println("5 - Ver escala de um turno");
            System.out.println("6 - Alterar tarefa de uma escala");
            System.out.println("7 - Remover voluntário da escala");
            System.out.println("8 - Relatório de carga horária dos voluntários");
            System.out.println("0 - Voltar");
            opcao = lerInt("Opção: ");
            try {
                switch (opcao) {
                    case 1 -> {
                        Turno t = new Turno(0, lerData("Data do turno (dd/MM/aaaa): "),
                                lerHora("Hora de início (HH:mm): "), lerHora("Hora de término (HH:mm): "),
                                lerTexto("Descrição: "));
                        turnoDAO.inserir(t);
                        System.out.println("Turno criado: " + t);
                    }
                    case 2 -> listar(turnoDAO.listar(), "Nenhum turno cadastrado.");
                    case 3 -> System.out.println(turnoDAO.excluir(lerInt("ID do turno: "))
                            ? "Turno excluído (e suas escalas)." : "Turno não encontrado.");
                    case 4 -> escalarVoluntario();
                    case 5 -> listar(escalaDAO.listarPorTurno(lerInt("ID do turno: ")), "Nenhum voluntário escalado.");
                    case 6 -> {
                        int idEscala = lerInt("ID da escala: ");
                        escalaDAO.atualizarTarefa(idEscala, lerTarefa());
                        System.out.println("Tarefa atualizada.");
                    }
                    case 7 -> System.out.println(escalaDAO.excluir(lerInt("ID da escala: "))
                            ? "Voluntário removido da escala." : "Escala não encontrada.");
                    case 8 -> listar(escalaDAO.relatorioCargaHoraria(), "Nenhum voluntário cadastrado.");
                    case 0 -> { }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Atenção: " + e.getMessage());
            } catch (SQLIntegrityConstraintViolationException e) {
                System.out.println("Operação não permitida: " + e.getMessage());
            }
        } while (opcao != 0);
    }

    private static void escalarVoluntario() throws SQLException {
        Turno turno = turnoDAO.buscarPorId(lerInt("ID do turno: "));
        if (turno == null) {
            System.out.println("Turno não encontrado.");
            return;
        }
        Usuario u = usuarioDAO.buscarPorId(lerInt("ID do voluntário: "));
        if (!(u instanceof Voluntario voluntario)) {
            System.out.println("Usuário não encontrado ou não é voluntário.");
            return;
        }
        Escala escala = new Escala(0, turno, voluntario, lerTarefa());
        escalaDAO.inserir(escala);
        System.out.println("Voluntário escalado: " + escala);
    }

    // ------------------------------------------------------------------ beneficiário   

    private static void menuBeneficiario(Beneficiario b, int opcao) throws SQLException {
        switch (opcao) {
            case 1 -> listar(safraDAO.listarDisponiveis(), "Nenhum alimento disponível no momento.");
            case 2 -> fazerReserva(b);
            case 3 -> listar(reservaDAO.listarPorBeneficiario(b), "Você ainda não possui reservas.");
            case 4 -> System.out.println(reservaDAO.cancelar(lerInt("ID da reserva: "), b.getIdUsuario())
                    ? "Reserva cancelada e quantidades devolvidas ao estoque."
                    : "Reserva não encontrada ou não está pendente.");
            default -> System.out.println("Opção inválida.");
        }
    }

    private static void fazerReserva(Beneficiario b) throws SQLException {
        List<Safra> disponiveis = safraDAO.listarDisponiveis();
        if (disponiveis.isEmpty()) {
            System.out.println("Nenhum alimento disponível no momento.");
            return;
        }
        disponiveis.forEach(System.out::println);

        Reserva reserva = new Reserva(0, b, LocalDateTime.now(), Reserva.PENDENTE);
        while (true) {
            int idSafra = lerInt("ID da safra para adicionar à cesta (0 para finalizar): ");
            if (idSafra == 0) break;
            Safra safra = disponiveis.stream().filter(s -> s.getIdSafra() == idSafra).findFirst().orElse(null);
            if (safra == null) {
                System.out.println("Safra indisponível.");
                continue;
            }
            try {
                reserva.adicionarItem(new ItemReserva(0, safra, lerDouble("Quantidade (kg): ")));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        if (reserva.getItens().isEmpty()) {
            System.out.println("Reserva não realizada (cesta vazia).");
            return;
        }
        try {
            reservaDAO.inserir(reserva);
            b.adicionarReserva(reserva);
            System.out.println("Reserva confirmada!\n" + reserva);
        } catch (SQLException e) {
            System.out.println("Reserva não realizada: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ utilitários de leitura

    private static void listar(List<?> itens, String mensagemVazia) {
        if (itens.isEmpty()) System.out.println(mensagemVazia);
        else itens.forEach(System.out::println);
    }

    private static String lerTexto(String msg) {
        System.out.print(msg);
        if (!sc.hasNextLine()) {
            // a entrada do console foi fechada (ex: programa executado sem terminal interativo)
            System.out.println("\nEntrada do teclado indisponível. Execute o programa em um terminal interativo.");
            System.exit(1);
        }
        return sc.nextLine().trim();
    }

    private static int lerInt(String msg) {
        while (true) {
            try {
                return Integer.parseInt(lerTexto(msg));
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    private static double lerDouble(String msg) {
        while (true) {
            try {
                return Double.parseDouble(lerTexto(msg).replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Digite um número válido.");
            }
        }
    }

    private static LocalDate lerDataOpcional(String msg, LocalDate atual) {
        while (true) {
            String txt = lerTexto(msg);
            if (txt.isBlank()) return atual;
            try {
                return LocalDate.parse(txt, FMT_DATA);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use o formato dd/MM/aaaa.");
            }
        }
    }

    private static LocalTime lerHora(String msg) {
        while (true) {
            try {
                return LocalTime.parse(lerTexto(msg));
            } catch (DateTimeParseException e) {
                System.out.println("Hora inválida. Use o formato HH:mm (ex: 07:30).");
            }
        }
    }

    private static Tarefa lerTarefa() {
        Tarefa[] tarefas = Tarefa.values();
        for (int i = 0; i < tarefas.length; i++) {
            System.out.println((i + 1) + " - " + tarefas[i].getDescricao());
        }
        while (true) {
            int op = lerInt("Tarefa: ");
            if (op >= 1 && op <= tarefas.length) return tarefas[op - 1];
            System.out.println("Tarefa inválida.");
        }
    }

    private static LocalDate lerData(String msg) {
        while (true) {
            try {
                return LocalDate.parse(lerTexto(msg), FMT_DATA);
            } catch (DateTimeParseException e) {
                System.out.println("Data inválida. Use o formato dd/MM/aaaa.");
            }
        }
    }
}
