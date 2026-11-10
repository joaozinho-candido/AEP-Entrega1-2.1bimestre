package br.com.fomezero.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Safra {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int idSafra;
    private Produtor produtor;
    private String nomeAlimento;
    private double quantidadeKg;
    private LocalDate dataColheita;
    private LocalDate dataValidade;

    public Safra(int idSafra, Produtor produtor, String nomeAlimento,
                 double quantidadeKg, LocalDate dataColheita, LocalDate dataValidade) {
        if (quantidadeKg < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa.");
        }
        validarDatas(dataColheita, dataValidade);
        this.idSafra = idSafra;
        this.produtor = produtor;
        this.nomeAlimento = nomeAlimento;
        this.quantidadeKg = quantidadeKg;
        this.dataColheita = dataColheita;
        this.dataValidade = dataValidade;
    }

    private static void validarDatas(LocalDate colheita, LocalDate validade) {
        if (validade.isBefore(colheita)) {
            throw new IllegalArgumentException("A data de validade não pode ser anterior à data de colheita.");
        }
    }

    /**
     * Altera o saldo da safra. Valor positivo = entrada, negativo = saída (reserva).
     * Não permite saldo negativo.
     */
    public void atualizarEstoque(double qtd) {
        double novoSaldo = quantidadeKg + qtd;
        if (novoSaldo < 0) {
            throw new IllegalArgumentException(String.format(
                    "Estoque insuficiente de %s: disponível %.2f kg, solicitado %.2f kg",
                    nomeAlimento, quantidadeKg, -qtd));
        }
        this.quantidadeKg = novoSaldo;
    }

    public boolean isVencida() {
        return dataValidade.isBefore(LocalDate.now());
    }

    public int getIdSafra() { return idSafra; }
    public void setIdSafra(int idSafra) { this.idSafra = idSafra; }

    public Produtor getProdutor() { return produtor; }
    public void setProdutor(Produtor produtor) { this.produtor = produtor; }

    public String getNomeAlimento() { return nomeAlimento; }
    public void setNomeAlimento(String nomeAlimento) { this.nomeAlimento = nomeAlimento; }

    public double getQuantidadeKg() { return quantidadeKg; }
    public void setQuantidadeKg(double quantidadeKg) { this.quantidadeKg = quantidadeKg; }

    public LocalDate getDataColheita() { return dataColheita; }
    public void setDataColheita(LocalDate dataColheita) {
        validarDatas(dataColheita, this.dataValidade);
        this.dataColheita = dataColheita;
    }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) {
        validarDatas(this.dataColheita, dataValidade);
        this.dataValidade = dataValidade;
    }

    @Override
    public String toString() {
        return String.format("[%d] %-15s %8.2f kg | colheita: %s | validade: %s | produtor: %s",
                idSafra, nomeAlimento, quantidadeKg,
                dataColheita.format(FMT), dataValidade.format(FMT),
                produtor != null ? produtor.getNome() : "-");
    }
}
