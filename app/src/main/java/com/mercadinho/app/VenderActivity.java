package com.mercadinho.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class VenderActivity extends Activity {
    private final Carrinho carrinho = new Carrinho();
    private final ArrayList<JSONObject> produtos = new ArrayList<>();
    private final ArrayList<JSONObject> produtosFiltrados = new ArrayList<>();
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private Spinner selecao;
    private EditText busca;
    private TextView resultadoBusca;
    private Button limparBusca;
    private boolean erroProdutos;
    private EditText quantidade;
    private LinearLayout lista;
    private TextView total;
    private Button adicionar;
    private Button finalizar;
    private String formaPagamento = Pagamento.DINHEIRO;
    private Pagamento pagamento;
    private TextView previaTroco;
    private boolean salvando;

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        LinearLayout tela = Visual.pagina(this);
        Visual.cabecalho(tela, "Nova venda", "Adicione os produtos e confira a compra.", () -> onBackPressed());
        LinearLayout formulario = Visual.cartao(tela);
        Visual.texto(formulario, "Adicionar produto", 22, Visual.TEXTO, true);
        TextView rotuloBusca = texto(formulario, "Buscar por nome ou código", 16);
        busca = new EditText(this);
        busca.setId(android.view.View.generateViewId());
        rotuloBusca.setLabelFor(busca.getId());
        busca.setSingleLine(true);
        busca.setInputType(InputType.TYPE_CLASS_TEXT);
        busca.setHint("Ex.: arroz ou 789123");
        Visual.campo(busca);
        Visual.inserir(formulario, busca, 8);
        limparBusca = Visual.botao(formulario, "Limpar busca", false);
        limparBusca.setOnClickListener(v -> busca.setText(""));
        resultadoBusca = Visual.texto(formulario, "", 16, Visual.SUAVE, false);
        TextView rotuloProduto = texto(formulario, "Escolha o produto", 16);
        selecao = new Spinner(this);
        selecao.setId(android.view.View.generateViewId());
        rotuloProduto.setLabelFor(selecao.getId());
        selecao.setMinimumHeight(Visual.dp(this, 64));
        selecao.setBackground(Visual.fundo(this, Visual.FUNDO, Visual.BORDA, 10));
        Visual.inserir(formulario, selecao, 8);
        TextView rotuloQuantidade = texto(formulario, "Quantidade", 16);
        quantidade = new EditText(this);
        quantidade.setId(android.view.View.generateViewId());
        rotuloQuantidade.setLabelFor(quantidade.getId());
        quantidade.setInputType(InputType.TYPE_CLASS_NUMBER);
        quantidade.setSingleLine(true);
        quantidade.setText("1");
        Visual.campo(quantidade);
        Visual.inserir(formulario, quantidade, 8);
        adicionar = Visual.botao(formulario, "+  Adicionar à compra", true);
        adicionar.setOnClickListener(v -> adicionarProduto());
        Visual.secao(tela, "Sua compra", "Confira os itens antes de finalizar.");
        lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
     …3305 tokens truncated…;
                    }
                }
                if (encontrado == null || encontrado.getInt("estoque") < item.quantidade
                        || encontrado.getLong("precoCentavos") != item.precoCentavos) {
                    avisar("O produto, preço ou estoque mudou. Remova o item e confira a compra.");
                    return;
                }
                encontrado.put("estoque", encontrado.getInt("estoque") - item.quantidade);
            }
            if (pagamento == null) {
                avisar("Escolha a forma de pagamento antes de confirmar.");
                return;
            }
            vendas.put(new JSONObject().put("id", UUID.randomUUID().toString())
                    .put("data", System.currentTimeMillis()).put("totalCentavos", carrinho.total())
                    .put("formaPagamento", pagamento.forma)
                    .put("valorRecebidoCentavos", pagamento.recebidoCentavos)
                    .put("trocoCentavos", pagamento.trocoCentavos).put("itens", itensJson()));
            // As duas alterações pertencem ao mesmo arquivo e à mesma gravação.
            boolean salvo = prefs.edit().putString("lista", estoque.toString())
                    .putString("vendas", vendas.toString()).commit();
            if (!salvo) {
                // commit também atualiza a memória quando a gravação em disco falha.
                prefs.edit().putString("lista", produtosAntes).putString("vendas", vendasAntes).commit();
                avisar("Não foi possível salvar a venda. Confira o espaço disponível no celular.");
                return;
            }
            carrinho.limpar();
            avisar("Venda salva. Estoque atualizado.");
        } catch (JSONException | ArithmeticException e) {
            avisar("Não foi possível finalizar: confira os dados salvos. A compra foi mantida.");
        } finally {
            salvando = false;
            carregarProdutos();
            atualizarCompra();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);
        estado.putString("busca", busca.getText().toString());
        estado.putString("produtoSelecionado", codigoSelecionado());
        estado.putString("quantidade", quantidade.getText().toString());
        try {
            estado.putString("carrinho", itensJson().toString());
        } catch (JSONException e) {
            avisar("Não foi possível guardar a compra em andamento.");
        }
    }

    private void restaurarCarrinho(String dados) {
        try {
            JSONArray itens = new JSONArray(dados);
            for (int i = 0; i < itens.length(); i++) {
                JSONObject item = itens.getJSONObject(i);
                carrinho.adicionar(item.getString("codigo"), item.getString("nome"),
                        item.getLong("precoCentavos"), item.getInt("quantidade"), Integer.MAX_VALUE);
            }
        } catch (JSONException | IllegalArgumentException | ArithmeticException e) {
            carrinho.limpar();
            avisar("Não foi possível recuperar a compra em andamento. Adicione os itens novamente.");
        }
    }

    @Override
    public void onBackPressed() {
        if (carrinho.vazio()) {
            super.onBackPressed();
            return;
        }
        new AlertDialog.Builder(this).setTitle("Sair da venda?")
                .setMessage("A compra em andamento será descartada. O estoque não será alterado.")
                .setNegativeButton("Continuar vendendo", null)
                .setPositiveButton("Descartar compra", (dialogo, botao) -> finish()).show();
    }
}
