package br.com.fomezero.model;

public class ItemReserva {

    private int idItem;
    private Safra safra;
    private double quantidadeReservadaKg;

    public ItemReserva(int idItem, Safra safra, double quantidadeReservadaKg) {
        if (quantidadeReservadaKg <= 0) {
            throw new IllegalArgumentException("A quantidade reservada deve ser maior que zero.");
        }
        this.idItem = idItem;
        this.safra = safra;
        this.quantidadeReservadaKg = quantidadeReservadaKg;
    }

    public int getIdItem() { return idItem; }
    public void setIdItem(int idItem) { this.idItem = idItem; }

    public Safra getSafra() { return safra; }
    public void setSafra(Safra safra) { this.safra = safra; }

    public double getQuantidadeReservadaKg() { return quantidadeReservadaKg; }
    public void setQuantidadeReservadaKg(double quantidadeReservadaKg) { this.quantidadeReservadaKg = quantidadeReservadaKg; }

    @Override
    public String toString() {
        return String.format("   - %s: %.2f kg", safra.getNomeAlimento(), quantidadeReservadaKg);
    }
}
