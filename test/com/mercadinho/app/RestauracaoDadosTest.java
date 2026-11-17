package com.mercadinho.app;

import java.util.Objects;

public final class RestauracaoDadosTest {
    private static final class Memoria implements RestauracaoDados.Armazenamento {
        String produtos = "produtos antigos";
        String vendas = "vendas antigas";
        int escritas;
        int falhas;
        public String ler(String chave) { return chave.equals("lista") ? produtos : vendas; }
        public boolean gravar(String p, String v) {
            produtos = p; vendas = v; escritas++;
            return escritas > falhas;
        }
    }

    private static void conferir(boolean ok) {
        if (!ok) throw new AssertionError();
    }

    public static void main(String[] args) {
        Memoria sucesso = new Memoria();
        conferir(RestauracaoDados.aplicar(sucesso, "novos", "novas") == RestauracaoDados.Resultado.SUCESSO);
        conferir(sucesso.produtos.equals("novos") && sucesso.vendas.equals("novas") && sucesso.escritas == 1);

        Memoria falha = new Memoria(); falha.falhas = 1;
        conferir(RestauracaoDados.aplicar(falha, "novos", "novas") == RestauracaoDados.Resultado.REVERTIDO);
        conferir(falha.produtos.equals("produtos antigos") && falha.vendas.equals("vendas antigas"));

        Memoria semDados = new Memoria(); semDados.produtos = null; semDados.vendas = null; semDados.falhas = 1;
        conferir(RestauracaoDados.aplicar(semDados, "[]", "[]") == RestauracaoDados.Resultado.REVERTIDO);
        conferir(Objects.isNull(semDados.produtos) && Objects.isNull(semDados.vendas));

        Memoria falhaDupla = new Memoria(); falhaDupla.falhas = 2;
        conferir(RestauracaoDados.aplicar(falhaDupla, "novos", "novas") == RestauracaoDados.Resultado.FALHA_AO_REVERTER);
        conferir(falhaDupla.escritas == 2);

        Memoria limpar = new Memoria();
        conferir(RestauracaoDados.aplicar(limpar, "[]", "[]") == RestauracaoDados.Resultado.SUCESSO);
        conferir(limpar.produtos.equals("[]") && limpar.vendas.equals("[]"));
        System.out.println("OK: 5 cenários de recuperação, reversão de falha e cópia vazia.");
    }
}
