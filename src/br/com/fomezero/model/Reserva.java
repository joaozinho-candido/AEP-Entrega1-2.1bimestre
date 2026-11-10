package br.com.fomezero.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Reserva {

    public static final String PENDENTE = "Pendente";
    public static final String CONCLUIDA = "Concluida";
    public static final String CANCELADA = "Cancelada";

    private int idReserva;
    private Beneficiario beneficiario;
    private LocalDateTime dataReserva;
    private String status;
    private final List<ItemReserva> itens = new ArrayList<>(); // composição 1 -> 1..N

    public Reserva(int idReserva, Beneficiario beneficiario, LocalDateTime dataReserva, String status) {
        this.idReserva = idReserva;
        this.beneficiario = beneficiario;
        this.dataReserva = dataReserva;
        this.status = status;
    }

    public void adicionarItem(ItemReserva item) {
        itens.add(item);
    }

    public double calcularTotalKg() {
        return itens.stream().mapToDouble(ItemReserva::getQuantidadeReservadaKg).sum();
    }

    public int getIdReserva() { return idReserva; }
    public void setIdReserva(int idReserva) { this.idReserva = idReserva; }

    public Beneficiario getBeneficiario() { return beneficiario; }
    public void setBeneficiario(Beneficiario beneficiario) { this.beneficiario = beneficiario; }

    public LocalDateTime getDataReserva() { return dataReserva; }
    public void setDataReserva(LocalDateTime dataReserva) { this.dataReserva = dataReserva; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<ItemReserva> getItens() { return itens; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Reserva #%d | %s | status: %s | total: %.2f kg%n",
                idReserva,
                dataReserva.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                status, calcularTotalKg()));
        for (ItemReserva item : itens) {
            sb.append(item).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
