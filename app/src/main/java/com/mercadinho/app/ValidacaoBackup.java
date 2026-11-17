package com.mercadinho.app;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Regras de integridade independentes do Android, executadas antes de gravar. */
final class ValidacaoBackup {
    static void conferir(Map<?, ?> copia) {
        if (!"com.mercadinho.app".equals(copia.get("aplicativo"))
                || inteiro(copia, "versaoFormato", 1, 1) != 1) invalido();
        inteiro(copia, "criadoEm", 0, Long.MAX_VALUE);
        Set<String> codigos = new HashSet<>();
        for (Object valor : lista(copia, "produtos")) {
            Map<?, ?> produto = objeto(valor);
            if (!codigos.add(texto(produto, "codigo"))) invalido();
            texto(produto, "nome");
            inteiro(produto, "precoCentavos", 0, Long.MAX_VALUE);
            inteiro(produto, "estoque", 0, Integer.MAX_VALUE);
        }
        Set<String> ids = new HashSet<>();
        for (Object valor : lista(copia, "vendas")) {
            Map<?, ?> venda = objeto(valor);
            if (!ids.add(texto(venda, "id"))) invalido();
            inteiro(venda, "data", 0, Long.MAX_VALUE);
            long total = inteiro(venda, "totalCentavos", 0, Long.MAX_VALUE);
            List<?> itens = lista(venda, "itens");
            if (itens.isEmpty()) invalido();
            Set<String> itensUnicos = new HashSet<>();
            long soma = 0;
            for (Object itemValor : itens) {
                Map<?, ?> item = objeto(itemValor);
                texto(item, "nome");
                if (!itensUnicos.add(texto(item, "codigo"))) invalido();
                long quantidade = inteiro(item, "quantidade", 1, Integer.MAX_VALUE);
                long preco = inteiro(item, "precoCentavos", 0, Long.MAX_VALUE);
                if (preco > Long.MAX_VALUE / quantidade) invalido();
                long subtotal = preco * quantidade;
                if (soma > Long.MAX_VALUE - subtotal) invalido();
                soma += subtotal;
            }
            if (total != soma) invalido();
            // Vendas anteriores à escolha da forma de pagamento continuam aceitas.
            if (venda.containsKey("formaPagamento")) {
                String forma = texto(venda, "formaPagamento");
                long recebido = inteiro(venda, "valorRecebidoCentavos", total, Long.MAX_VALUE);
                long troco = inteiro(venda, "trocoCentavos", 0, Long.MAX_VALUE);
                if (troco != recebido - total) invalido();
                if (!"Dinheiro".equals(forma)) {
                    if ((!"Pix".equals(forma) && !"Cartão".equals(forma)) || troco != 0) invalido();
                }
            } else if (venda.containsKey("valorRecebidoCentavos") || venda.containsKey("trocoCentavos")) {
                invalido();
            }
        }
    }

    private static Map<?, ?> objeto(Object valor) {
        if (!(valor instanceof Map)) invalido();
        return (Map<?, ?>) valor;
    }

    private static List<?> lista(Map<?, ?> objeto, String chave) {
        Object valor = objeto.get(chave);
        if (!(valor instanceof List)) invalido();
        return (List<?>) valor;
    }

    private static String texto(Map<?, ?> objeto, String chave) {
        Object valor = objeto.get(chave);
        if (!(valor instanceof String) || ((String) valor).trim().isEmpty()) invalido();
        return (String) valor;
    }

    private static long inteiro(Map<?, ?> objeto, String chave, long minimo, long maximo) {
        Object valor = objeto.get(chave);
        if (!(valor instanceof Integer) && !(valor instanceof Long)) invalido();
        long numero = ((Number) valor).longValue();
        if (numero < minimo || numero > maximo) invalido();
        return numero;
    }

    private static void invalido() {
        throw new IllegalArgumentException("A cópia está incompleta, possui dados inválidos ou usa um formato incompatível.");
    }
}
