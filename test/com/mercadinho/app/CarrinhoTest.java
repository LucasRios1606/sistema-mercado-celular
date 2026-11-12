package com.mercadinho.app;

public final class CarrinhoTest {
    private static void conferir(boolean condicao) {
        if (!condicao) throw new AssertionError();
    }

    private static void rejeitar(Runnable acao) {
        try {
            acao.run();
            throw new AssertionError("A operação deveria ser rejeitada");
        } catch (IllegalArgumentException | ArithmeticException esperado) {
            // Quantidade, estoque e valores inválidos não podem mudar a compra.
        }
    }

    public static void main(String[] args) {
        Carrinho c = new Carrinho();
        conferir(c.vazio() && c.total() == 0);
        c.adicionar("001", "Arroz", 599, 2, 3);
        c.adicionar("002", "Feijão", 750, 1, 4);
        conferir(c.total() == 1948);
        c.adicionar("001", "Arroz", 599, 1, 3);
        conferir(c.total() == 2547 && c.itens().size() == 2);
        rejeitar(() -> c.adicionar("001", "Arroz", 599, 1, 3));
        rejeitar(() -> c.adicionar("002", "Feijão", 750, 0, 4));
        rejeitar(() -> c.adicionar("002", "Feijão", 750, -1, 4));
        rejeitar(() -> c.adicionar("003", "Sem estoque", 100, 1, 0));
        rejeitar(() -> c.adicionar("002", "Feijão", 800, 1, 4));
        conferir(c.total() == 2547);
        c.remover("001");
        conferir(c.total() == 750);
        c.limpar();
        conferir(c.vazio() && c.total() == 0);
        c.adicionar("001", "Produto", 10, Integer.MAX_VALUE, Integer.MAX_VALUE);
        rejeitar(() -> c.adicionar("001", "Produto", 10, 1, Integer.MAX_VALUE));
        c.limpar();
        rejeitar(() -> c.adicionar("001", "Produto", Long.MAX_VALUE, 2, 2));
        conferir(c.vazio());
        c.adicionar("001", "Produto", Long.MAX_VALUE, 1, 1);
        rejeitar(() -> c.adicionar("002", "Outro", 1, 1, 1));
        conferir(c.total() == Long.MAX_VALUE && c.itens().size() == 1);
        System.out.println("OK: totais em centavos, soma de quantidades, remoção, estoque e limites numéricos.");
    }
}
