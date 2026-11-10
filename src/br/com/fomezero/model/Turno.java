package br.com.fomezero.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Turno de trabalho na horta comunitária, criado pela administração (RF04). */
public class Turno {

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int idTurno;
    private LocalDate dataTurno;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String descricao;
    private final List<Escala> escalas = new ArrayList<>(); // 1 Turno -> N Escalas

    public Turno(int idTurno, LocalDate dataTurno, LocalTime horaInicio, LocalTime horaFim, String descricao) {
        if (!horaFim.isAfter(horaInicio)) {
            throw new IllegalArgumentException("A hora de término deve ser posterior à hora de início.");
        }
        this.idTurno = idTurno;
        this.dataTurno = dataTurno;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.descricao = descricao;
    }

    public double calcularDuracaoHoras() {
        return Duration.between(horaInicio, horaFim).toMinutes() / 60.0;
    }

    public void adicionarEscala(Escala escala) { escalas.add(escala); }
    public List<Escala> getEscalas() { return escalas; }

    public int getIdTurno() { return idTurno; }
    public void setIdTurno(int idTurno) { this.idTurno = idTurno; }

    public LocalDate getDataTurno() { return dataTurno; }
    public void setDataTurno(LocalDate dataTurno) { this.dataTurno = dataTurno; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    @Override
    public String toString() {
        return String.format("Turno #%d | %s das %s às %s (%.1f h) | %s",
                idTurno, dataTurno.format(FMT_DATA), horaInicio, horaFim,
                calcularDuracaoHoras(), descricao == null ? "" : descricao);
    }
}
