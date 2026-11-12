package com.mercadinho.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout tela = Visual.pagina(this);
        TextView marca = Visual.texto(tela, "MERCADINHO", 16, Visual.VERDE, true);
        marca.setLetterSpacing(0.16f);
        marca.setPadding(0, Visual.dp(this, 16), 0, Visual.dp(this, 24));
        Visual.texto(tela, "Tudo pronto\npara atender.", 36, Visual.TEXTO, true);
        Visual.texto(tela, "O dia a dia do mercado, na palma da mão.", 17, Visual.SUAVE, false);
        Visual.secao(tela, "O que vamos fazer?", "");
        acao(tela, "Nova venda", "Escolher produtos e fechar a compra", true)
                .setOnClickListener(v -> startActivity(new Intent(this, VenderActivity.class)));
        acao(tela, "Produtos", "Cadastrar e consultar o estoque", false)
                .setOnClickListener(v -> startActivity(new Intent(this, ProdutosActivity.class)));
        Button historico = acao(tela, "Vendas de hoje", "Em breve", false);
        historico.setEnabled(false);
        TextView rodape = Visual.texto(tela, "Seus dados ficam salvos neste celular.", 14, Visual.SUAVE, false);
        rodape.setPadding(0, Visual.dp(this, 24), 0, 0);
        rodape.setGravity(Gravity.CENTER);
    }

    private Button acao(LinearLayout tela, String titulo, String descricao, boolean principal) {
        Button botao = Visual.botao(tela, titulo, principal);
        SpannableString texto = new SpannableString(titulo + "\n" + descricao);
        texto.setSpan(new RelativeSizeSpan(25f / 18f), 0, titulo.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        texto.setSpan(new RelativeSizeSpan(16f / 18f), titulo.length() + 1, texto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        botao.setText(texto);
        botao.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        botao.setMinHeight(Visual.dp(this, 112));
        botao.setPadding(Visual.dp(this, 22), Visual.dp(this, 20), Visual.dp(this, 22), Visual.dp(this, 20));
        botao.setLineSpacing(Visual.dp(this, 6), 1);
        return botao;
    }
}
