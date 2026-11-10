package br.com.fomezero.model;

/** Atribuição de um Voluntário a uma Tarefa em um Turno (RF04). */
public class Escala {

    private int idEscala;
    private Turno turno;
    private Voluntario voluntario;
    private Tarefa tarefa;

    public Escala(int idEscala, Turno turno, Voluntario voluntario, Tarefa tarefa) {
        this.idEscala = idEscala;
        this.turno = turno;
        this.voluntario = voluntario;
        this.tarefa = tarefa;
    }

    public int getIdEscala() { return idEscala; }
    public void setIdEscala(int idEscala) { this.idEscala = idEscala; }

    public Turno getTurno() { return turno; }
    public void setTurno(Turno turno) { this.turno = turno; }

    public Voluntario getVoluntario() { return voluntario; }
    public void setVoluntario(Voluntario voluntario) { this.voluntario = voluntario; }

    public Tarefa getTarefa() { return tarefa; }
    public void setTarefa(Tarefa tarefa) { this.tarefa = tarefa; }

    @Override
    public String toString() {
        return String.format("Escala #%d | %s | %s -> %s",
                idEscala, turno, voluntario.getNome(), tarefa.getDescricao());
    }
}
