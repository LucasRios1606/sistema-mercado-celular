package com.mercadinho.app;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class ValoresProduto {
    static long precoEmCentavos(String texto) {
        String valor = texto.trim().replace(',', '.');
        if (!valor.matches("[0-9]+([.][0-9]{1,2})?")) {
            throw new IllegalArgumentException("Informe o preço com até duas casas decimais. Ex.: 12,50.");
        }
        try {
            return new BigDecimal(valor).setScale(2, RoundingMode.UNNECESSARY)
                    .movePointRight(2).longValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("O preço informado é muito alto.");
        }
    }

    static int repor(int atual, String texto) {
        int entrada;
        try {
            entrada = Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Informe uma quantidade inteira maior que zero.");
        }
        if (atual < 0 || entrada <= 0) {
            throw new IllegalArgumentException("Informe uma quantidade inteira maior que zero.");
        }
        if (entrada > Integer.MAX_VALUE - atual) {
            throw new IllegalArgumentException("A quantidade ultrapassa o limite do estoque.");
        }
        return atual + entrada;
    }
}
