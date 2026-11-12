package com.mercadinho.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private Spinner selecao;
    private EditText quantidade;
    private LinearLayout lista;
    private TextView total;
    private Button adicionar;
    private Button finalizar;
    private boolean salvando;

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        LinearLayout tela = Visual.pagina(this);
        Visual.cabecalho(tela, "Nova venda", "Adicione os produtos e confira a compra.", () -> onBackPressed());
        LinearLayout formulario = Visual.cartao(tela);
        Visual.texto(formulario, "Adicionar produto", 22, Visual.TEXTO, true);
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
        tela.addView(lista);
        LinearLayout resumo = Visual.cartao(tela);
        resumo.setBackground(Visual.fundo(this, Visual.VERDE, 0, 18));
        Visual.texto(resumo, "TOTAL DA COMPRA", 14, android.graphics.Color.WHITE, true);
        total = Visual.texto(resumo, "", 34, android.graphics.Color.WHITE, true);
        finalizar = Visual.botao(tela, "Finalizar venda", true);
        finalizar.setOnClickListener(v -> confirmarVenda());
        carregarProdutos();
        if (estado != null) restaurarCarrinho(estado.getString("carrinho", "[]"));
        atualizarCompra();
    }

    private SharedPreferences preferencias() {
        return getSharedPreferences("produtos", MODE_PRIVATE);
    }

    private TextView texto(LinearLayout tela, String valor, int tamanho) {
        TextView texto = Visual.texto(tela, valor, tamanho, Visual.TEXTO, true);
        texto.setPadding(0, Visual.dp(this, 12), 0, 0);
        return texto;
    }

    private Button botao(LinearLayout tela, String rotulo) {
        return Visual.botao(tela, rotulo, false);
    }

    private String dinheiro(long centavos) {
        return moeda.format(BigDecimal.valueOf(centavos, 2));
    }

    private void avisar(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

    private void carregarProdutos() {
        produtos.clear();
        ArrayList<String> opcoes = new ArrayList<>();
        try {
            JSONArray dados = new JSONArray(preferencias().getString("lista", "[]"));
            for (int i = 0; i < dados.length(); i++) {
                JSONObject produto = dados.getJSONObject(i);
                String rotulo = produto.getString("nome") + " · "
                        + dinheiro(produto.getLong("precoCentavos")) + " · Estoque: "
                        + produto.getInt("estoque") + " · " + produto.getString("codigo");
                produtos.add(produto);
                opcoes.add(rotulo);
            }
        } catch (JSONException e) {
            produtos.clear();
            opcoes.clear();
            avisar("Não foi possível ler os produtos salvos.");
        }
        if (produtos.isEmpty()) opcoes.add("Nenhum produto disponível. Cadastre em Produtos.");
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this,
                R.layout.produto_selecionado, opcoes);
        adaptador.setDropDownViewResource(R.layout.produto_opcao);
        selecao.setAdapter(adaptador);
        adicionar.setEnabled(!produtos.isEmpty());
        selecao.setEnabled(!produtos.isEmpty());
    }

    private void adicionarProduto() {
        int posicao = selecao.getSelectedItemPosition();
        if (posicao < 0 || posicao >= produtos.size()) return;
        try {
            JSONObject produto = produtos.get(posicao);
            int unidades = Integer.parseInt(quantidade.getText().toString().trim());
            carrinho.adicionar(produto.getString("codigo"), produto.getString("nome"),
                    produto.getLong("precoCentavos"), unidades, produto.getInt("estoque"));
            quantidade.setText("1");
            atualizarCompra();
        } catch (NumberFormatException e) {
            avisar("Informe uma quantidade inteira maior que zero.");
        } catch (IllegalArgumentException e) {
            avisar(e.getMessage());
        } catch (JSONException | ArithmeticException e) {
            avisar("Não foi possível adicionar este produto. Confira os valores.");
        }
    }

    private void atualizarCompra() {
        lista.removeAllViews();
        if (carrinho.vazio()) Visual.vazio(lista, "A compra começa aqui", "Escolha um produto acima e toque em Adicionar à compra.");
        for (Carrinho.Item item : carrinho.itens()) {
            LinearLayout cartao = Visual.cartao(lista);
            Visual.texto(cartao, item.nome, 22, Visual.TEXTO, true);
            Visual.texto(cartao, item.quantidade + " × " + dinheiro(item.precoCentavos), 17, Visual.SUAVE, false);
            Visual.texto(cartao, dinheiro(item.subtotal()), 26, Visual.VERDE, true);
            Button remover = botao(cartao, "Remover item");
            remover.setContentDescription("Remover " + item.nome);
            remover.setOnClickListener(v -> {
                carrinho.remover(item.codigo);
                atualizarCompra();
            });
        }
        total.setText(dinheiro(carrinho.total()));
        finalizar.setEnabled(!carrinho.vazio() && !salvando);
    }

    private JSONArray itensJson() throws JSONException {
        JSONArray itens = new JSONArray();
        for (Carrinho.Item item : carrinho.itens()) {
            itens.put(new JSONObject().put("codigo", item.codigo).put("nome", item.nome)
                    .put("precoCentavos", item.precoCentavos).put("quantidade", item.quantidade));
        }
        return itens;
    }

    private void confirmarVenda() {
        if (carrinho.vazio() || salvando) return;
        new AlertDialog.Builder(this).setTitle("Finalizar venda?")
                .setMessage("Total: " + dinheiro(carrinho.total())
                        + "\nA venda será salva e os itens serão descontados do estoque.")
                .setNegativeButton("Voltar", null)
                .setPositiveButton("Confirmar venda", (dialogo, botao) -> salvarVenda()).show();
    }

    private void salvarVenda() {
        if (salvando || carrinho.vazio()) return;
        salvando = true;
        finalizar.setEnabled(false);
        SharedPreferences prefs = preferencias();
        String produtosAntes = prefs.getString("lista", "[]");
        String vendasAntes = prefs.getString("vendas", "[]");
        try {
            JSONArray estoque = new JSONArray(produtosAntes);
            JSONArray vendas = new JSONArray(vendasAntes);
            for (Carrinho.Item item : carrinho.itens()) {
                JSONObject encontrado = null;
                for (int i = 0; i < estoque.length(); i++) {
                    JSONObject produto = estoque.getJSONObject(i);
                    if (item.codigo.equals(produto.getString("codigo"))) {
                        if (encontrado != null) throw new JSONException("Código duplicado");
                        encontrado = produto;
                    }
                }
                if (encontrado == null || encontrado.getInt("estoque") < item.quantidade
                        || encontrado.getLong("precoCentavos") != item.precoCentavos) {
                    avisar("O produto, preço ou estoque mudou. Remova o item e confira a compra.");
                    return;
                }
                encontrado.put("estoque", encontrado.getInt("estoque") - item.quantidade);
            }
            vendas.put(new JSONObject().put("id", UUID.randomUUID().toString())
                    .put("data", System.currentTimeMillis()).put("totalCentavos", carrinho.total())
                    .put("itens", itensJson()));
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
