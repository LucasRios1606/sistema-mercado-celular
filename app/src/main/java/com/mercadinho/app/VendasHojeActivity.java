package com.mercadinho.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class VendasHojeActivity extends Activity {
    private final Locale portugues = new Locale("pt", "BR");
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(portugues);
    private final Handler relogio = new Handler(Looper.getMainLooper());
    private final Runnable virarDia = () -> atualizar();

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizar();
    }

    @Override
    protected void onPause() {
        relogio.removeCallbacks(virarDia);
        super.onPause();
    }

    private String dinheiro(long centavos) {
        return moeda.format(BigDecimal.valueOf(centavos, 2));
    }

    private void atualizar() {
        relogio.removeCallbacks(virarDia);
        long agora = System.currentTimeMillis();
        TimeZone fuso = TimeZone.getDefault();
        VendasDoDia resumo = new VendasDoDia(agora, fuso);
        SimpleDateFormat data = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", portugues);
        SimpleDateFormat horario = new SimpleDateFormat("HH:mm", portugues);
        data.setTimeZone(fuso);
        horario.setTimeZone(fuso);
        LinearLayout tela = Visual.pagina(this);
        Visual.cabecalho(tela, "Vendas de hoje", data.format(new Date(agora)), () -> finish());
        Visual.botao(tela, "Atualizar", false).setOnClickListener(v -> atualizar());
        relogio.postDelayed(virarDia, Math.max(1000, resumo.fim - agora + 100));

        ArrayList<Registro> vendas = new ArrayList<>();
        try {
            JSONArray salvas = new JSONArray(getSharedPreferences("produtos", MODE_PRIVATE)
                    .getString("vendas", "[]"));
            for (int i = 0; i < salvas.length(); i++) {
                JSONObject venda = salvas.getJSONObject(i);
                long instante = venda.getLong("data");
                if (!resumo.contem(instante)) continue;
                long valor = venda.getLong("totalCentavos");
                JSONArray itens = venda.getJSONArray("itens");
                if (itens.length() == 0) throw new JSONException("Venda sem itens");
                long somaItens = 0;
                ArrayList<String> detalhes = new ArrayList<>();
                for (int j = 0; j < itens.length(); j++) {
                    JSONObject item = itens.getJSONObject(j);
                    int unidades = item.getInt("quantidade");
                    long preco = item.getLong("precoCentavos");
                    if (unidades <= 0 || preco < 0 || preco > Long.MAX_VALUE / unidades) {
                        throw new JSONException("Valor inválido");
                    }
                    long subtotal = preco * unidades;
                    if (somaItens > Long.MAX_VALUE - subtotal) throw new JSONException("Total inválido");
                    somaItens += subtotal;
                    detalhes.add(item.getString("nome") + "\n" + unidades + " × "
                            + dinheiro(preco) + " = " + dinheiro(subtotal));
                }
                if (somaItens != valor) throw new JSONException("Total divergente");
                resumo.adicionar(valor);
                vendas.add(new Registro(instante, valor, detalhes));
            }
        } catch (JSONException | IllegalArgumentException e) {
            Visual.vazio(tela, "Não foi possível consultar as vendas",
                    "Há um problema nos dados salvos. O total do dia não pôde ser calculado. Seus registros foram mantidos.");
            return;
        }

        LinearLayout total = Visual.cartao(tela);
        total.setBackground(Visual.fundo(this, Visual.VERDE, 0, 18));
        Visual.texto(total, "TOTAL VENDIDO HOJE", 14, Color.WHITE, true);
        Visual.texto(total, dinheiro(resumo.total()), 34, Color.WHITE, true);
        Visual.texto(total, resumo.quantidade() + (resumo.quantidade() == 1
                ? " venda concluída" : " vendas concluídas"), 17, Color.WHITE, false);

        if (vendas.isEmpty()) {
            Visual.vazio(tela, "Nenhuma venda por enquanto",
                    "As compras finalizadas em Nova venda aparecerão aqui.");
            return;
        }
        Visual.secao(tela, "Vendas realizadas", "Da mais recente para a mais antiga.");
        Collections.sort(vendas, (a, b) -> Long.compare(b.data, a.data));
        for (Registro venda : vendas) {
            LinearLayout cartao = Visual.cartao(tela);
            Visual.texto(cartao, "Venda às " + horario.format(new Date(venda.data)), 22, Visual.TEXTO, true);
            Visual.texto(cartao, dinheiro(venda.total), 26, Visual.VERDE, true);
            for (String item : venda.itens) {
                android.widget.TextView linha = Visual.texto(cartao, item, 17, Visual.TEXTO, false);
                linha.setPadding(0, Visual.dp(this, 10), 0, 0);
            }
        }
    }

    private static final class Registro {
        final long data;
        final long total;
        final ArrayList<String> itens;

        Registro(long data, long total, ArrayList<String> itens) {
            this.data = data;
            this.total = total;
            this.itens = itens;
        }
    }
}
