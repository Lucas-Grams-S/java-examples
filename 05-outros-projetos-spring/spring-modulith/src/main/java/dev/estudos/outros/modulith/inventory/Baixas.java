package dev.estudos.outros.modulith.inventory;

import java.util.List;

/** Leitura (pública) das baixas registradas, para teste. */
public final class Baixas {
    private Baixas() {}
    public static List<String> todas() { return List.copyOf(StockListener.BAIXAS); }
    public static void limpar() { StockListener.BAIXAS.clear(); }
}
