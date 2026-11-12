package com.mercadinho.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ScrollView;

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
            long precoCentavos = ValoresProduto.precoEmCentavos(precoTexto);
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
            if (!gravarProdutos(produtos)) return;
            nome.setText("");
            codigo.setText("");
            preco.setText("");
            quantidade.setText("");
            atualizarLista();
            Toast.makeText(this, R.string.produto_salvo, Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            avisar(e instanceof NumberFormatException ? "Confira a quantidade em estoque." : e.getMessage());
        } catch (JSONException e) {
            avisar("Não foi possível ler os produtos. Os dados salvos foram mantidos.");
        }
    }

    private JSONArray lerProdutos() throws JSONException {
        String dados = getSharedPreferences(PREFERENCIAS, MODE_PRIVATE)
                .getString(CHAVE_LISTA, "[]");
        return new JSONArray(dados);
    }

    private void atualizarLista() {
        lista.removeAllViews();
        JSONArray produtos;
        try {
            produtos = lerProdutos();
        } catch (JSONException e) {
            Visual.vazio(lista, "Não foi possível ler os produtos", "Os dados salvos foram mantidos. Tente abrir esta tela novamente.");
            return;
        }
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
                String codigoOriginal = produto.getString("codigo");
                Visual.botao(cartao, "Editar produto", false)
                        .setOnClickListener(v -> editarProduto(codigoOriginal));
                Visual.botao(cartao, "Repor estoque", false)
                        .setOnClickListener(v -> reporEstoque(codigoOriginal));
            } catch (JSONException ignored) {
                // Ignora apenas registros locais inválidos.
            }
        }
    }

    private void avisar(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

    private JSONObject encontrar(JSONArray produtos, String codigoOriginal) throws JSONException {
        JSONObject encontrado = null;
        for (int i = 0; i < produtos.length(); i++) {
            JSONObject produto = produtos.getJSONObject(i);
            if (produto.getString("codigo").equals(codigoOriginal)) {
                if (encontrado != null) throw new JSONException("Código duplicado");
                encontrado = produto;
            }
        }
        if (encontrado == null) throw new JSONException("Produto não encontrado");
        return encontrado;
    }

    private boolean gravarProdutos(JSONArray produtos) {
        SharedPreferences prefs = getSharedPreferences(PREFERENCIAS, MODE_PRIVATE);
        String anterior = prefs.getString(CHAVE_LISTA, "[]");
        if (!prefs.edit().putString(CHAVE_LISTA, produtos.toString()).commit()) {
            prefs.edit().putString(CHAVE_LISTA, anterior).commit();
            avisar("Não foi possível salvar. Confira o espaço disponível no celular.");
            return false;
        }
        return true;
    }

    private LinearLayout formularioDialogo() {
        LinearLayout formulario = Visual.coluna(this);
        int margem = Visual.dp(this, 20);
        formulario.setPadding(margem, margem, margem, margem);
        return formulario;
    }

    private AlertDialog dialogo(String titulo, LinearLayout formulario, String acao) {
        ScrollView rolagem = new ScrollView(this);
        rolagem.addView(formulario);
        AlertDialog dialogo = new AlertDialog.Builder(this).setTitle(titulo)
                .setView(rolagem).setNegativeButton("Cancelar", null)
                .setPositiveButton(acao, null).create();
        dialogo.setCanceledOnTouchOutside(false);
        return dialogo;
    }

    private void editarProduto(String codigoOriginal) {
        try {
            JSONObject produto = encontrar(lerProdutos(), codigoOriginal);
            LinearLayout formulario = formularioDialogo();
            EditText novoNome = campo(formulario, R.string.nome_produto, "textCapSentences");
            EditText novoCodigo = campo(formulario, R.string.codigo_produto, "number");
            EditText novoPreco = campo(formulario, R.string.preco_produto, "numberDecimal");
            novoNome.setText(produto.getString("nome"));
            novoCodigo.setText(produto.getString("codigo"));
            novoPreco.setText(java.math.BigDecimal.valueOf(produto.getLong("precoCentavos"), 2)
                    .toPlainString().replace('.', ','));
            Visual.texto(formulario, "O novo preço vale para as próximas vendas. As vendas já realizadas ficam como foram registradas.",
                    15, Visual.SUAVE, false);
            AlertDialog dialogo = dialogo("Editar produto", formulario, "Salvar alterações");
            dialogo.setOnShowListener(ignorado -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String nomeEditado = novoNome.getText().toString().trim();
                        String codigoEditado = novoCodigo.getText().toString().trim();
                        if (nomeEditado.isEmpty() || codigoEditado.isEmpty()) {
                            avisar("Preencha o nome e o código do produto.");
                            return;
                        }
                        try {
                            long valor = ValoresProduto.precoEmCentavos(novoPreco.getText().toString());
                            JSONArray produtos = lerProdutos();
                            JSONObject atual = encontrar(produtos, codigoOriginal);
                            for (int i = 0; i < produtos.length(); i++) {
                                JSONObject outro = produtos.getJSONObject(i);
                                if (outro != atual && outro.getString("codigo").equals(codigoEditado)) {
                                    avisar(getString(R.string.codigo_ja_cadastrado));
                                    return;
                                }
                            }
                            atual.put("nome", nomeEditado);
                            atual.put("codigo", codigoEditado);
                            atual.put("precoCentavos", valor);
                            if (!gravarProdutos(produtos)) return;
                            atualizarLista();
                            dialogo.dismiss();
                            avisar("Produto atualizado.");
                        } catch (IllegalArgumentException e) {
                            avisar(e.getMessage());
                        } catch (JSONException e) {
                            avisar("Não foi possível atualizar o produto. Os dados salvos foram mantidos.");
                        }
                    }));
            dialogo.show();
        } catch (JSONException e) {
            avisar("Não foi possível abrir este produto.");
        }
    }

    private void reporEstoque(String codigoOriginal) {
        try {
            JSONObject produto = encontrar(lerProdutos(), codigoOriginal);
            LinearLayout formulario = formularioDialogo();
            Visual.texto(formulario, produto.getString("nome"), 22, Visual.TEXTO, true);
            int estoque = produto.getInt("estoque");
            Visual.texto(formulario, "Estoque atual: " + estoque, 18, Visual.SUAVE, false);
            EditText entrada = campo(formulario, R.string.quantidade_recebida, "number");
            entrada.setHint("Ex.: 10");
            TextView previa = Visual.texto(formulario, "Informe quantas unidades chegaram.", 18, Visual.VERDE, true);
            entrada.addTextChangedListener(new android.text.TextWatcher() {
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        previa.setText("Estoque após a entrada: " + ValoresProduto.repor(estoque, s.toString()));
                    } catch (IllegalArgumentException e) {
                        previa.setText("Informe uma quantidade válida de unidades recebidas.");
                    }
                }
                public void afterTextChanged(android.text.Editable s) { }
            });
            AlertDialog dialogo = dialogo("Repor estoque", formulario, "Adicionar ao estoque");
            dialogo.setOnShowListener(ignorado -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        try {
                            JSONArray produtos = lerProdutos();
                            JSONObject atual = encontrar(produtos, codigoOriginal);
                            int novoEstoque = ValoresProduto.repor(atual.getInt("estoque"), entrada.getText().toString());
                            atual.put("estoque", novoEstoque);
                            if (!gravarProdutos(produtos)) return;
                            atualizarLista();
                            dialogo.dismiss();
                            avisar("Estoque atualizado: " + novoEstoque + " unidades.");
                        } catch (IllegalArgumentException e) {
                            avisar(e.getMessage());
                        } catch (JSONException e) {
                            avisar("Não foi possível repor o estoque. Os dados salvos foram mantidos.");
                        }
                    }));
            dialogo.show();
        } catch (JSONException e) {
            avisar("Não foi possível abrir o estoque deste produto.");
        }
    }
}
