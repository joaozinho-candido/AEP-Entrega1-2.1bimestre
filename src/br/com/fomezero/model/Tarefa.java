package br.com.fomezero.model;

/** Tarefas da horta comunitária (RF04). Espelha o ENUM tb_escala.tarefa. */
public enum Tarefa {
    PLANTIO("Plantio"),
    REGA("Rega"),
    COLHEITA("Colheita"),
    MONTAGEM_CESTAS("Montagem de cestas");

    private final String descricao;

    Tarefa(String descricao) { this.descricao = descricao; }

    public String getDescricao() { return descricao; }
}
