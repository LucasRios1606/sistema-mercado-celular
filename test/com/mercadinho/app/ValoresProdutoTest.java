package com.mercadinho.app;

public final class ValoresProdutoTest {
    private static void conferir(boolean condicao) {
        if (!condicao) throw new AssertionError();
    }

    private static void rejeitar(Runnable acao) {
        try {
            acao.run();
            throw new AssertionError("Valor inválido aceito");
        } catch (IllegalArgumentException esperado) { }
    }

    public static void main(String[] args) {
        conferir(ValoresProduto.precoEmCentavos("12,50") == 1250);
        conferir(ValoresProduto.precoEmCentavos(" 0.29 ") == 29);
        conferir(ValoresProduto.precoEmCentavos("5") == 500);
        conferir(ValoresProduto.precoEmCentavos("5,5") == 550);
        conferir(ValoresProduto.precoEmCentavos("0,00") == 0);
        conferir(ValoresProduto.precoEmCentavos("92233720368547758,07") == Long.MAX_VALUE);
        for (String invalido : new String[]{"", "-1", "NaN", "Infinity", "1e3", "1,234",
                "1.234,56", "92233720368547758,08", "abc"}) {
            rejeitar(() -> ValoresProduto.precoEmCentavos(invalido));
        }
        conferir(ValoresProduto.repor(5, "10") == 15);
        conferir(ValoresProduto.repor(0, " 1 ") == 1);
        conferir(ValoresProduto.repor(Integer.MAX_VALUE - 1, "1") == Integer.MAX_VALUE);
        for (String invalido : new String[]{"", "0", "-1", "1,5", "1.5", "abc", "2147483648"}) {
            rejeitar(() -> ValoresProduto.repor(5, invalido));
        }
        rejeitar(() -> ValoresProduto.repor(Integer.MAX_VALUE, "1"));
        rejeitar(() -> ValoresProduto.repor(-1, "10"));
        System.out.println("OK: preços exatos em centavos, reposição de estoque e rejeição de valores inválidos.");
    }
}
