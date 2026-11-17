package com.mercadinho.app;

final class RestauracaoDados {
    interface Armazenamento {
        String ler(String chave);
        boolean gravar(String produtos, String vendas);
    }

    enum Resultado { SUCESSO, REVERTIDO, FALHA_AO_REVERTER }

    static Resultado aplicar(Armazenamento destino, String produtos, String vendas) {
        String produtosAntes = destino.ler("lista");
        String vendasAntes = destino.ler("vendas");
        if (destino.gravar(produtos, vendas)) return Resultado.SUCESSO;
        // A escrita pode atualizar a memória mesmo quando retorna false.
        return destino.gravar(produtosAntes, vendasAntes) ? Resultado.REVERTIDO : Resultado.FALHA_AO_REVERTER;
    }
}
