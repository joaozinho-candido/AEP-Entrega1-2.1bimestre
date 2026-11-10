package br.com.fomezero.model;

public class Insumo {

    private int idInsumo;
    private String nomeInsumo;
    private int quantidadeEstoque;

    public Insumo(int idInsumo, String nomeInsumo, int quantidadeEstoque) {
        this.idInsumo = idInsumo;
        this.nomeInsumo = nomeInsumo;
        this.quantidadeEstoque = quantidadeEstoque;
    }

    /** Baixa no estoque (RF05). */
    public void darBaixaEstoque(int qtd) {
        if (qtd <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        if (qtd > quantidadeEstoque) {
            throw new IllegalArgumentException("Estoque insuficiente de " + nomeInsumo
                    + ": disponível " + quantidadeEstoque + ", solicitado " + qtd);
        }
        quantidadeEstoque -= qtd;
    }

    /** Entrada no estoque (RF05). */
    public void adicionarEstoque(int qtd) {
        if (qtd <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        quantidadeEstoque += qtd;
    }

    public int getIdInsumo() { return idInsumo; }
    public void setIdInsumo(int idInsumo) { this.idInsumo = idInsumo; }

    public String getNomeInsumo() { return nomeInsumo; }
    public void setNomeInsumo(String nomeInsumo) { this.nomeInsumo = nomeInsumo; }

    public int getQuantidadeEstoque() { return quantidadeEstoque; }
    public void setQuantidadeEstoque(int quantidadeEstoque) { this.quantidadeEstoque = quantidadeEstoque; }

    @Override
    public String toString() {
        return String.format("[%d] %-30s estoque: %d", idInsumo, nomeInsumo, quantidadeEstoque);
    }
}
