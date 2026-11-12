package com.mercadinho.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Elementos visuais compartilhados pelas telas do mercadinho. */
final class Visual {
    static final int VERDE = Color.rgb(28, 91, 67);
    static final int TEXTO = Color.rgb(30, 49, 43);
    static final int SUAVE = Color.rgb(91, 109, 100);
    static final int FUNDO = Color.rgb(245, 247, 243);
    static final int BORDA = Color.rgb(216, 225, 218);

    static int dp(Context contexto, int valor) {
        return Math.round(valor * contexto.getResources().getDisplayMetrics().density);
    }

    static GradientDrawable fundo(Context contexto, int cor, int borda, int raio) {
        GradientDrawable desenho = new GradientDrawable();
        desenho.setColor(cor);
        desenho.setCornerRadius(dp(contexto, raio));
        if (borda != 0) desenho.setStroke(dp(contexto, 1), borda);
        return desenho;
    }

    static LinearLayout coluna(Context contexto) {
        LinearLayout coluna = new LinearLayout(contexto);
        coluna.setOrientation(LinearLayout.VERTICAL);
        return coluna;
    }

    static void inserir(LinearLayout pai, View filho, int margemTopo) {
        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        parametros.topMargin = dp(pai.getContext(), margemTopo);
        pai.addView(filho, parametros);
    }

    static LinearLayout pagina(Activity atividade) {
        ScrollView rolagem = new ScrollView(atividade);
        rolagem.setFillViewport(true);
        rolagem.setBackgroundColor(FUNDO);
        LinearLayout tela = coluna(atividade);
        int margem = dp(atividade, 20);
        tela.setPadding(margem, margem, margem, dp(atividade, 32));
        rolagem.addView(tela);
        atividade.setContentView(rolagem);
        return tela;
    }

    static TextView texto(LinearLayout pai, String valor, int tamanho, int cor, boolean negrito) {
        TextView texto = new TextView(pai.getContext());
        texto.setText(valor);
        texto.setTextSize(tamanho);
        texto.setTextColor(cor);
        texto.setFontFeatureSettings("tnum");
        if (negrito) texto.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        texto.setLineSpacing(dp(pai.getContext(), 3), 1);
        inserir(pai, texto, 4);
        return texto;
    }

    static void cabecalho(LinearLayout tela, String titulo, String subtitulo, Runnable voltar) {
        Button retorno = botao(tela, "‹  Início", false);
        retorno.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        retorno.setContentDescription("Voltar ao início");
        retorno.setOnClickListener(v -> voltar.run());
        texto(tela, titulo, 32, TEXTO, true);
        texto(tela, subtitulo, 16, SUAVE, false);
    }

    static LinearLayout cartao(LinearLayout pai) {
        LinearLayout cartao = coluna(pai.getContext());
        int margem = dp(pai.getContext(), 18);
        cartao.setPadding(margem, margem, margem, margem);
        cartao.setBackground(fundo(pai.getContext(), Color.WHITE, BORDA, 18));
        inserir(pai, cartao, 16);
        return cartao;
    }

    static void secao(LinearLayout pai, String titulo, String descricao) {
        TextView texto = texto(pai, titulo, 22, TEXTO, true);
        texto.setPadding(0, dp(pai.getContext(), 20), 0, 0);
        if (!descricao.isEmpty()) texto(pai, descricao, 15, SUAVE, false);
    }

    static void campo(EditText campo) {
        Context contexto = campo.getContext();
        campo.setTextSize(20);
        campo.setTextColor(TEXTO);
        campo.setHintTextColor(SUAVE);
        campo.setMinHeight(dp(contexto, 58));
        campo.setPadding(dp(contexto, 14), dp(contexto, 12), dp(contexto, 14), dp(contexto, 12));
        android.graphics.drawable.StateListDrawable estados = new android.graphics.drawable.StateListDrawable();
        estados.addState(new int[]{android.R.attr.state_focused}, fundo(contexto, Color.WHITE, VERDE, 10));
        estados.addState(new int[]{}, fundo(contexto, FUNDO, BORDA, 10));
        campo.setBackground(estados);
        campo.setBackgroundTintList(null);
    }

    static Button botao(LinearLayout pai, String rotulo, boolean principal) {
        Context contexto = pai.getContext();
        Button botao = new Button(contexto);
        botao.setText(rotulo);
        botao.setTextSize(18);
        botao.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        botao.setAllCaps(false);
        botao.setMinHeight(dp(contexto, 58));
        botao.setPadding(dp(contexto, 16), dp(contexto, 12), dp(contexto, 16), dp(contexto, 12));
        int[][] estados = {new int[]{-android.R.attr.state_enabled}, new int[]{}};
        botao.setTextColor(new ColorStateList(estados,
                new int[]{SUAVE, principal ? Color.WHITE : VERDE}));
        botao.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22477760),
                fundo(contexto, principal ? VERDE : Color.WHITE, principal ? 0 : BORDA, 12), null));
        botao.setBackgroundTintList(new ColorStateList(estados,
                new int[]{BORDA, principal ? VERDE : Color.WHITE}));
        botao.setStateListAnimator(null);
        inserir(pai, botao, 12);
        return botao;
    }

    static void vazio(LinearLayout pai, String titulo, String descricao) {
        LinearLayout cartao = cartao(pai);
        texto(cartao, titulo, 20, TEXTO, true);
        texto(cartao, descricao, 16, SUAVE, false);
    }
}
