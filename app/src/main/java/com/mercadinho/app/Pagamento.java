package com.mercadinho.app;

final class Pagamento {
    static final String DINHEIRO = "Dinheiro";
    static final String PIX = "Pix";
    static final String CARTAO = "Cartão";

    final String forma;
    final long recebidoCentavos;
    final long trocoCentavos;

    private Pagamento(String forma, long recebidoCentavos, long trocoCentavos) {
        this.forma = forma;
        this.recebidoCentavos = recebidoCentavos;
        this.trocoCentavos = trocoCentavos;
    }

    static Pagamento criar(String forma, long totalCentavos, String valorRecebido) {
        if (totalCentavos < 0) throw new IllegalArgumentException("Total inválido.");
        if (DINHEIRO.equals(forma)) {
            long recebido = ValoresProduto.precoEmCentavos(valorRecebido);
            if (recebido < totalCentavos) {
                throw new IllegalArgumentException("O valor recebido não cobre o total da compra.");
            }
            return new Pagamento(forma, recebido, recebido - totalCentavos);
        }
        if (PIX.equals(forma) || CARTAO.equals(forma)) return new Pagamento(forma, totalCentavos, 0);
        throw new IllegalArgumentException("Escolha dinheiro, Pix ou cartão.");
    }
}
