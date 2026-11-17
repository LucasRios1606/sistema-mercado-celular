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
    private static final int CRIAR_BACKUP = 100;
    private static final java.util.concurrent.atomic.AtomicBoolean exportando =
            new java.util.concurrent.atomic.AtomicBoolean(false);
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
        acao(tela, "Vendas de hoje", "Conferir as vendas e o total do dia", false)
                .setOnClickListener(v -> startActivity(new Intent(this, VendasHojeActivity.class)));
        acao(tela, "Salvar cópia dos dados", "Exportar produtos, estoque e vendas", false)
                .setOnClickListener(v -> explicarBackup());
        acao(tela, "Recuperar cópia", "Restaurar dados de um arquivo salvo", false)
                .setOnClickListener(v -> startActivity(new Intent(this, RestaurarBackupActivity.class)));
        TextView rodape = Visual.texto(tela, "Seus dados ficam salvos neste celular.", 14, Visual.SUAVE, false);
        rodape.setPadding(0, Visual.dp(this, 24), 0, 0);
        rodape.setGravity(Gravity.CENTER);
    }

    private void explicarBackup() {
        if (exportando.get()) {
            android.widget.Toast.makeText(this, "Aguarde a cópia terminar.", android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        new android.app.AlertDialog.Builder(this)
                .setTitle("Salvar cópia dos dados")
                .setMessage("Você vai escolher onde guardar um arquivo com todos os produtos, o estoque e as vendas. "
                        + "Os dados do aplicativo continuam como estão.\n\n"
                        + "Para usar este arquivo depois, escolha Recuperar cópia na tela inicial.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Escolher local", (dialogo, botao) -> escolherLocalBackup())
                .show();
    }

    private void escolherLocalBackup() {
        String data = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", java.util.Locale.ROOT)
                .format(new java.util.Date());
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/json")
                .putExtra(Intent.EXTRA_TITLE, "mercadinho-backup-" + data + ".json");
        try {
            startActivityForResult(intent, CRIAR_BACKUP);
        } catch (android.content.ActivityNotFoundException e) {
            android.widget.Toast.makeText(this, "Não foi possível abrir o seletor de arquivos neste celular.",
                    android.widget.Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int pedido, int resultado, Intent dados) {
        super.onActivityResult(pedido, resultado, dados);
        if (pedido != CRIAR_BACKUP || resultado != RESULT_OK || dados == null || dados.getData() == null) return;
        if (!exportando.compareAndSet(false, true)) return;
        android.net.Uri destino = dados.getData();
        android.content.Context contexto = getApplicationContext();
        android.os.Handler principal = new android.os.Handler(android.os.Looper.getMainLooper());
        android.widget.Toast.makeText(this, "Salvando cópia dos dados…", android.widget.Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            String mensagem;
            try {
                // Uma única leitura mantém produtos e vendas no mesmo retrato dos dados.
                java.util.Map<String, ?> salvos = contexto.getSharedPreferences("produtos", MODE_PRIVATE).getAll();
                Object produtos = salvos.get("lista");
                Object vendas = salvos.get("vendas");
                org.json.JSONObject copia = new org.json.JSONObject()
                        .put("aplicativo", "com.mercadinho.app")
                        .put("versaoFormato", 1)
                        .put("criadoEm", System.currentTimeMillis())
                        .put("produtos", new org.json.JSONArray(produtos == null ? "[]" : (String) produtos))
                        .put("vendas", new org.json.JSONArray(vendas == null ? "[]" : (String) vendas));
                byte[] conteudo = copia.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                try (java.io.OutputStream saida = contexto.getContentResolver().openOutputStream(destino, "wt")) {
                    if (saida == null) throw new java.io.IOException("Destino indisponível");
                    saida.write(conteudo);
                    saida.flush();
                }
                mensagem = "Cópia salva no local escolhido.";
            } catch (org.json.JSONException | ClassCastException e) {
                mensagem = "Não foi possível criar a cópia: há dados inválidos no aplicativo. Os dados originais foram mantidos.";
            } catch (java.io.IOException | RuntimeException e) {
                mensagem = "Não foi possível concluir a cópia. O arquivo escolhido pode estar incompleto; tente salvar novamente. Os dados originais foram mantidos.";
            } finally {
                exportando.set(false);
            }
            String aviso = mensagem;
            principal.post(() -> android.widget.Toast.makeText(contexto, aviso, android.widget.Toast.LENGTH_LONG).show());
        }, "exportar-backup").start();
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
