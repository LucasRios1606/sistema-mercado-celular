package com.mercadinho.app;

public final class PagamentoTest {
    private static void conferir(boolean condicao) {
        if (!condicao) throw new AssertionError();
    }

    private static void rejeitar(Runnable acao) {
        try {
            acao.run();
            throw new AssertionError("Pagamento inválido aceito");
        } catch (IllegalArgumentException esperado) { }
    }

    public static void main(String[] args) {
        Pagamento exato = Pagamento.criar(Pagamento.DINHEIRO, 1299, "12,99");
        conferir(exato.recebidoCentavos == 1299 && exato.trocoCentavos == 0);
        Pagamento troco = Pagamento.criar(Pagamento.DINHEIRO, 1299, "20,00");
        conferir(troco.recebidoCentavos == 2000 && troco.trocoCentavos == 701);
        Pagamento pix = Pagamento.criar(Pagamento.PIX, 1299, "");
        conferir(pix.forma.equals("Pix") && pix.recebidoCentavos == 1299 && pix.trocoCentavos == 0);
        Pagamento cartao = Pagamento.criar(Pagamento.CARTAO, 1299, "");
        conferir(cartao.forma.equals("Cartão") && cartao.trocoCentavos == 0);
        Pagamento zero = Pagamento.criar(Pagamento.DINHEIRO, 0, "0");
        conferir(zero.trocoCentavos == 0);
        rejeitar(() -> Pagamento.criar(Pagamento.DINHEIRO, 1300, "12,99"));
        rejeitar(() -> Pagamento.criar(Pagamento.DINHEIRO, 1, ""));
        rejeitar(() -> Pagamento.criar(Pagamento.DINHEIRO, 1, "1,009"));
        rejeitar(() -> Pagamento.criar("Cheque", 100, ""));
        rejeitar(() -> Pagamento.criar(Pagamento.PIX, -1, ""));
        System.out.println("OK: pagamentos em dinheiro, Pix e cartão; troco e valores inválidos.");
    }
}
