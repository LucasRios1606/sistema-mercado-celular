package com.mercadinho.app;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Compra em andamento: não altera o estoque dos produtos. */
public final class Carrinho {
    public static final class Item {
        public final String codigo;
        public final String nome;
        public final long precoCentavos;
        public final int quantidade;

        Item(String codigo, String nome, long precoCentavos, int quantidade) {
            this.codigo = codigo;
            this.nome = nome;
            this.precoCentavos = precoCentavos;
            this.quantidade = quantidade;
        }

        public long subtotal() {
            if (precoCentavos > Long.MAX_VALUE / quantidade) throw new ArithmeticException();
            return precoCentavos * quantidade;
        }
    }

    private final Map<String, Item> itens = new LinkedHashMap<>();

    public void adicionar(String codigo, String nome, long preco, int quantidade, int estoque) {
        if (codigo == null || codigo.isEmpty() || nome == null || preco < 0 || quantidade <= 0) {
            throw new IllegalArgumentException("Confira o produto e a quantidade.");
        }
        Item anterior = itens.get(codigo);
        long novaQuantidade = (long) quantidade + (anterior == null ? 0 : anterior.quantidade);
        if (novaQuantidade > estoque) {
            throw new IllegalArgumentException("Quantidade maior que o estoque disponível.");
        }
        if (anterior != null && anterior.precoCentavos != preco) {
            throw new IllegalArgumentException("O preço mudou. Remova o item e adicione novamente.");
        }
        Item novo = new Item(codigo, nome, preco, (int) novaQuantidade);
        long outros = total() - (anterior == null ? 0 : anterior.subtotal());
        somar(outros, novo.subtotal());
        itens.put(codigo, novo);
    }

    public List<Item> itens() { return new ArrayList<>(itens.values()); }
    public void remover(String codigo) { itens.remove(codigo); }
    public void limpar() { itens.clear(); }
    public boolean vazio() { return itens.isEmpty(); }

    public long total() {
        long total = 0;
        for (Item item : itens.values()) total = somar(total, item.subtotal());
        return total;
    }

    private static long somar(long a, long b) {
        if (a > Long.MAX_VALUE - b) throw new ArithmeticException();
        return a + b;
    }
}
