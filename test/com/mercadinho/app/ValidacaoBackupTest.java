package com.mercadinho.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ValidacaoBackupTest {
    private static int casos;

    private static Map<String, Object> objeto(Object... valores) {
        Map<String, Object> resultado = new HashMap<>();
        for (int i = 0; i < valores.length; i += 2) resultado.put((String) valores[i], valores[i + 1]);
        return resultado;
    }

    private static Map<String, Object> copia() {
        Map<String, Object> produto = objeto("nome", "Feijão", "codigo", "001", "precoCentavos", 250L, "estoque", 3);
        Map<String, Object> item = objeto("nome", "Feijão antigo", "codigo", "001", "precoCentavos", 200L, "quantidade", 2);
        Map<String, Object> venda = objeto("id", "venda-1", "data", 1700000000000L, "totalCentavos", 400L,
                "itens", new ArrayList<>(Arrays.asList(item)), "formaPagamento", "Dinheiro",
                "valorRecebidoCentavos", 500L, "trocoCentavos", 100L);
        return objeto("aplicativo", "com.mercadinho.app", "versaoFormato", 1, "criadoEm", 1700000001000L,
                "produtos", new ArrayList<>(Arrays.asList(produto)), "vendas", new ArrayList<>(Arrays.asList(venda)));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> lista(Map<String, Object> copia, String chave) {
        return (List<Map<String, Object>>) copia.get(chave);
    }
    private static Map<String, Object> produto(Map<String, Object> c) { return lista(c, "produtos").get(0); }
    private static Map<String, Object> venda(Map<String, Object> c) { return lista(c, "vendas").get(0); }
    private static Map<String, Object> item(Map<String, Object> c) { return lista(venda(c), "itens").get(0); }

    private static void rejeita(Consumer<Map<String, Object>> alterar) {
        Map<String, Object> c = copia();
        alterar.accept(c);
        try {
            ValidacaoBackup.conferir(c);
            throw new AssertionError("Cópia inválida foi aceita: " + c);
        } catch (IllegalArgumentException esperado) { casos++; }
    }

    private static void aceita(Map<String, Object> c) {
        String antes = c.toString();
        ValidacaoBackup.conferir(c);
        if (!antes.equals(c.toString())) throw new AssertionError("Validação modificou dados");
        casos++;
    }

    public static void main(String[] args) {
        aceita(copia());
        Map<String, Object> vazia = copia();
        lista(vazia, "produtos").clear(); lista(vazia, "vendas").clear(); aceita(vazia);
        Map<String, Object> antiga = copia();
        venda(antiga).remove("formaPagamento"); venda(antiga).remove("valorRecebidoCentavos");
        venda(antiga).remove("trocoCentavos"); aceita(antiga);
        Map<String, Object> codigoEditado = copia();
        produto(codigoEditado).put("codigo", "novo-codigo"); aceita(codigoEditado);
        for (String forma : Arrays.asList("Pix", "Cartão")) {
            Map<String, Object> c = copia(); venda(c).put("formaPagamento", forma);
            venda(c).put("valorRecebidoCentavos", 400L); venda(c).put("trocoCentavos", 0L); aceita(c);
        }
        rejeita(c -> c.put("aplicativo", "outro.app"));
        rejeita(c -> c.put("versaoFormato", 2));
        rejeita(c -> c.put("versaoFormato", "1"));
        rejeita(c -> c.remove("criadoEm"));
        rejeita(c -> c.put("criadoEm", -1L));
        rejeita(c -> c.remove("produtos"));
        rejeita(c -> c.put("vendas", "[]"));
        rejeita(c -> lista(c, "produtos").add(new HashMap<>(produto(c))));
        rejeita(c -> produto(c).put("nome", " "));
        rejeita(c -> produto(c).put("codigo", 123));
        rejeita(c -> produto(c).put("estoque", -1));
        rejeita(c -> produto(c).put("estoque", (long) Integer.MAX_VALUE + 1));
        rejeita(c -> produto(c).put("estoque", 1.5));
        rejeita(c -> produto(c).put("precoCentavos", -1L));
        rejeita(c -> produto(c).put("precoCentavos", "250"));
        rejeita(c -> lista(c, "vendas").add(new HashMap<>(venda(c))));
        rejeita(c -> venda(c).put("data", -1));
        rejeita(c -> venda(c).put("id", ""));
        rejeita(c -> lista(venda(c), "itens").clear());
        rejeita(c -> item(c).put("quantidade", 0));
        rejeita(c -> item(c).put("quantidade", (long) Integer.MAX_VALUE + 1));
        rejeita(c -> item(c).put("precoCentavos", Long.MAX_VALUE));
        rejeita(c -> venda(c).put("totalCentavos", 401L));
        rejeita(c -> lista(venda(c), "itens").add(new HashMap<>(item(c))));
        rejeita(c -> venda(c).put("formaPagamento", "Fiado"));
        rejeita(c -> venda(c).put("valorRecebidoCentavos", 399L));
        rejeita(c -> venda(c).put("trocoCentavos", 99L));
        rejeita(c -> venda(c).put("formaPagamento", "Pix"));
        rejeita(c -> venda(c).remove("valorRecebidoCentavos"));
        rejeita(c -> venda(c).remove("formaPagamento"));
        rejeita(c -> {
            item(c).put("precoCentavos", Long.MAX_VALUE); item(c).put("quantidade", 1);
            lista(venda(c), "itens").add(objeto("nome", "Outro", "codigo", "002", "precoCentavos", 1L, "quantidade", 1));
        });
        System.out.println("OK: " + casos + " casos de integridade da cópia, compatibilidade e limites numéricos.");
    }
}
