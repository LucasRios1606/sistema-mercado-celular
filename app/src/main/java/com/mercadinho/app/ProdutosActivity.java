package com.mercadinho.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

        LinearLayout tela = Visual.pagina(this);
        Visual.cabecalho(tela, "Produtos", "Organize os produtos do seu mercadinho.", () -> finish());
        LinearLayout formulario = Visual.cartao(tela);
        Visual.texto(formulario, "Cadastrar produto", 22, Visual.TEXTO, true);
        nome = campo(formulario, R.string.nome_produto, "textCapSentences");
        codigo = campo(formulario, R.string.codigo_produto, "number");
        preco = campo(formulario, R.string.preco_produto, "numberDecimal");
        quantidade = campo(formulario, R.string.quantidade_produto, "number");

        Button salvar = Visual.botao(formulario, getString(R.string.salvar_produto), true);
        salvar.setOnClickListener(v -> salvarProduto());

        Visual.secao(tela, "Produtos cadastrados", "Preços e quantidades disponíveis.");

        lista = new LinearLayout(this);
        lista.setOrientation(LinearLayout.VERTICAL);
        tela.addView(lista, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        atualizarLista();
    }

    private EditText campo(LinearLayout tela, int rotulo, String tipo) {
        EditText campo = new EditText(this);
        campo.setId(android.view.View.generateViewId());
        TextView etiqueta = Visual.texto(tela, getString(rotulo), 16, Visual.TEXTO, true);
        etiqueta.setPadding(0, Visual.dp(this, 12), 0, Visual.dp(this, 4));
        etiqueta.setLabelFor(campo.getId());
        campo.setSingleLine(true);
        campo.setHint(rotulo == R.string.nome_produto ? "Ex.: Arroz 5 kg"
                : rotulo == R.string.preco_produto ? "0,00"
                : rotulo == R.string.quantidade_produto ? "0" : "Digite o código");
        int entrada = tipo.equals("textCapSentences")
                ? 0x00002001 : tipo.equals("numberDecimal") ? 0x00002002 : 0x00000002;
        campo.setInputType(entrada);
        Visual.campo(campo);
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
            Visual.vazio(lista, "Seu primeiro produto", "Preencha os campos acima para começar o cadastro.");
            return;
        }
        for (int i = 0; i < produtos.length(); i++) {
            try {
                JSONObject produto = produtos.getJSONObject(i);
                LinearLayout cartao = Visual.cartao(lista);
                Visual.texto(cartao, produto.getString("nome"), 22, Visual.TEXTO, true);
                String valor = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt", "BR"))
                        .format(java.math.BigDecimal.valueOf(produto.getLong("precoCentavos"), 2));
                Visual.texto(cartao, valor, 26, Visual.VERDE, true);
                Visual.texto(cartao, "Estoque: " + produto.getInt("estoque"), 17, Visual.TEXTO, false);
                Visual.texto(cartao, "Código " + produto.getString("codigo"), 14, Visual.SUAVE, false);
            } catch (JSONException ignored) {
                // Ignora apenas registros locais inválidos.
            }
        }
    }
}
