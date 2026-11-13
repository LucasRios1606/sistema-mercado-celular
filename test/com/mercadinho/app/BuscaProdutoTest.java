package com.mercadinho.app;

import java.util.Locale;

public final class BuscaProdutoTest {
    private static void conferir(boolean condicao) {
        if (!condicao) throw new AssertionError();
    }

    public static void main(String[] args) {
        conferir(BuscaProduto.corresponde("Arroz 5 kg", "001789", "arroz"));
        conferir(BuscaProduto.corresponde("Feijão preto", "001789", "FEIJAO"));
        conferir(BuscaProduto.corresponde("Açúcar cristal", "001789", " açucar "));
        conferir(BuscaProduto.corresponde("Leite  integral", "001789", "leite integral"));
        conferir(BuscaProduto.corresponde("Arroz", "001789", "001"));
        conferir(BuscaProduto.corresponde("Arroz", "001789", "1789"));
        conferir(BuscaProduto.corresponde("Arroz", "001789", "001789"));
        conferir(BuscaProduto.corresponde("Arroz", "001789", "   "));
        conferir(BuscaProduto.corresponde("Arroz", "001789", ""));
        conferir(!BuscaProduto.corresponde("Arroz", "001789", "feijao"));
        conferir(!BuscaProduto.corresponde("Arroz", "001789", "0017890"));
        conferir(!BuscaProduto.corresponde("Arroz", "001789", "roz001"));
        conferir(!BuscaProduto.corresponde("Arroz", "001789", ".*"));
        Locale anterior = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            conferir(BuscaProduto.corresponde("LEITE", "0001", "leite"));
        } finally {
            Locale.setDefault(anterior);
        }
        System.out.println("OK: busca por nome e código, acentos, maiúsculas, espaços e ausência de resultados.");
    }
}
