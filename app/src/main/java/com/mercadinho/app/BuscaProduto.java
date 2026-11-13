package com.mercadinho.app;

import java.text.Normalizer;
import java.util.Locale;

final class BuscaProduto {
    static boolean corresponde(String nome, String codigo, String consulta) {
        String termo = normalizar(consulta);
        return termo.isEmpty() || normalizar(nome).contains(termo)
                || normalizar(codigo).contains(termo);
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").trim().replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}
