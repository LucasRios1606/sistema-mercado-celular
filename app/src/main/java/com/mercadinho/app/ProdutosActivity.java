package com.mercadinho.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProdutosActivity extends Activity {
    private static final String PREFERENCIAS = "produtos";
    private static final String CHAVE_LISTA = "lista";
    private LinearLayout lista;
    private EditText nome;
    private EditText codigo;
    private EditText preco;
    private EditText quantidade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView rolagem = new ScrollView(this);
        LinearLayout tela = new LinearLayout(this);
        tela.setOrientation(LinearLayout.VERTICAL);
        int margem = (int) (24 * getResources().getDisplayMetrics().density);
        tela.setPadding(margem, margem, margem, margem);
        rolagem.addView(tela);

        TextView titulo = new TextView(this);
        titulo.setText(R.string.produtos);
        titulo.setTextSize(28);
        tela.addView(titulo);

        nome = campo(tela, R.string.nome_produto, "textCapSentences");
        codigo = campo(tela, R.string.codigo_produto, "number");
        preco = campo(tela, R.string.preco_produto, "numberDecimal");
        quantidade = campo(tela, R.string.quantidade_produto, "number");

        Button salvar = new Button(this);
        salvar.setText(R.string.salvar_produto);
        tela.addView(salvar);
        salvar.setOnClickListener(v -> salvarProduto());

        TextView tituloLista = new TextView(this);
        tituloLista.setText(R.string.produtos_cadastrados);
        tituloLista.setTextSize(22);
        tituloLista.setPadding(0, margem, 0, 8);
        tela.addView(tituloLista);

        lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        tela.addView(lista, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(rolagem);
        atualizarLista();
    }

    private EditText campo(LinearLayout tela, int rotulo, String tipo) {
        EditText campo = new EditText(this);
        campo.setSingleLine(true);
        campo.setHint(rotulo);
        int entrada = tipo.equals("textCapSentences")
                ? 0x00002001 : tipo.equals("numberDecimal") ? 0x00002002 : 0x00000002;
        campo.setInputType(entrada);
        tela.addView(campo, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return campo;
    }

    private void salvarProduto() {
        String nomeProduto = nome.getText().toString().trim();
        String codigoProduto = codigo.getText().toString().trim();
        String precoTexto = preco.getText().toString().trim().replace(',', '.');
        String quantidadeTexto = quantidade.getText().toString().trim();
        if (nomeProduto.isEmpty() || codigoProduto.isEmpty()
                || precoTexto.isEmpty() || quantidadeTexto.isEmpty()) {
            Toast.makeText(this, R.string.preencha_todos_campos, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long precoCentavos = Math.round(Double.parseDouble(precoTexto) * 100);
            int estoque = Integer.parseInt(quantidadeTexto);
            if (precoCentavos < 0 || estoque < 0) throw new NumberFormatException();

            JSONArray produtos = lerProdutos();
            for (int i = 0; i < produtos.length(); i++) {
                if (produtos.getJSONObject(i).getString("codigo").equals(codigoProduto)) {
                    Toast.makeText(this, R.string.codigo_ja_cadastrado, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            JSONObject produto = new JSONObject();
            produto.put("nome", nomeProduto);
            produto.put("codigo", codigoProduto);
            produto.put("precoCentavos", precoCentavos);
            produto.put("estoque", estoque);
            produtos.put(produto);
            getSharedPreferences(PREFERENCIAS, MODE_PRIVATE).edit()
                    .putString(CHAVE_LISTA, produtos.toString()).apply();
            nome.setText("");
            codigo.setText("");
            preco.setText("");
            quantidade.setText("");
            atualizarLista();
            Toast.makeText(this, R.string.produto_salvo, Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException | JSONException e) {
            Toast.makeText(this, R.string.valores_invalidos, Toast.LENGTH_SHORT).show();
        }
    }

    private JSONArray lerProdutos() {
        String dados = getSharedPreferences(PREFERENCIAS, MODE_PRIVATE)
                .getString(CHAVE_LISTA, "[]");
        try {
            return new JSONArray(dados);
        } catch (JSONException e) {
            return new JSONArray();
        }
    }

    private void atualizarLista() {
        lista.removeAllViews();
        JSONArray produtos = lerProdutos();
        if (produtos.length() == 0) {
            TextView vazio = new TextView(this);
            vazio.setText(R.string.nenhum_produto);
            lista.addView(vazio);
            return;
        }
        for (int i = 0; i < produtos.length(); i++) {
            try {
                JSONObject produto = produtos.getJSONObject(i);
                TextView item = new TextView(this);
                item.setText(getString(R.string.resumo_produto,
                        produto.getString("nome"), produto.getString("codigo"),
                        produto.getInt("estoque")));
                item.setTextSize(18);
                item.setPadding(0, 12, 0, 12);
                lista.addView(item);
            } catch (JSONException ignored) {
                // Ignora apenas registros locais inválidos.
            }
        }
    }
}
