package com.mercadinho.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

public class RestaurarBackupActivity extends Activity {
    private static final int ESCOLHER_COPIA = 101;
    private Trabalho trabalho;
    private TextView resumo;
    private Button escolher;
    private Button restaurar;

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        trabalho = (Trabalho) getLastNonConfigurationInstance();
        if (trabalho == null) trabalho = new Trabalho();
        trabalho.tela = this;
        LinearLayout tela = Visual.pagina(this);
        Visual.cabecalho(tela, "Recuperar cópia", "Use um arquivo salvo pelo Mercadinho.", () -> onBackPressed());
        Visual.texto(tela, "A recuperação substitui todos os produtos, o estoque e as vendas deste celular. "
                + "Salve uma cópia dos dados atuais antes de continuar.", 18, Visual.TEXTO, false);
        escolher = Visual.botao(tela, "Escolher arquivo", false);
        escolher.setOnClickListener(v -> escolherArquivo());
        resumo = Visual.texto(Visual.cartao(tela), "", 18, Visual.TEXTO, false);
        restaurar = Visual.botao(tela, "Recuperar esta cópia", true);
        restaurar.setOnClickListener(v -> confirmar());
        mostrar();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return trabalho;
    }

    @Override
    protected void onDestroy() {
        if (trabalho.tela == this) trabalho.tela = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (trabalho.ocupado) {
            Toast.makeText(this, "Aguarde a operação terminar.", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    private void mostrar() {
        escolher.setEnabled(!trabalho.ocupado);
        restaurar.setEnabled(!trabalho.ocupado && trabalho.copia != null);
        String texto = trabalho.mensagem;
        if (trabalho.copia != null && !trabalho.ocupado) {
            ArquivoBackup copia = trabalho.copia;
            texto += "\n\nCópia de " + DateFormat.getDateTimeInstance().format(new Date(copia.criadoEm))
                    + "\nProdutos: " + copia.quantidadeProdutos + "\nVendas: " + copia.quantidadeVendas;
            if (copia.quantidadeProdutos == 0 && copia.quantidadeVendas == 0) {
                texto += "\nEsta cópia está vazia. Recuperá-la deixará o cadastro e as vendas vazios.";
            }
        }
        resumo.setText(texto);
    }

    private void escolherArquivo() {
        if (trabalho.ocupado) return;
        trabalho.copia = null;
        trabalho.mensagem = "Escolha a cópia que deseja recuperar.";
        mostrar();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                .setType("*/*");
        try {
            startActivityForResult(intent, ESCOLHER_COPIA);
        } catch (android.content.ActivityNotFoundException e) {
            trabalho.mensagem = "Não foi possível abrir o seletor de arquivos neste celular.";
            mostrar();
        }
    }

    @Override
    protected void onActivityResult(int pedido, int resultado, Intent dados) {
        super.onActivityResult(pedido, resultado, dados);
        if (pedido == ESCOLHER_COPIA && resultado == RESULT_OK && dados != null && dados.getData() != null
                && !trabalho.ocupado) {
            trabalho.ler(getApplicationContext(), dados.getData());
        }
    }

    private void confirmar() {
        if (trabalho.ocupado || trabalho.copia == null) return;
        new AlertDialog.Builder(this).setTitle("Substituir os dados atuais?")
                .setMessage("Serão recuperados " + trabalho.copia.quantidadeProdutos + " produtos e "
                        + trabalho.copia.quantidadeVendas + " vendas. Os produtos, o estoque e as vendas atuais "
                        + "serão substituídos pelos dados desta cópia. Esta operação não junta os cadastros.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Substituir e recuperar", (dialogo, botao) -> {
                    if (!trabalho.ocupado && trabalho.copia != null) trabalho.gravar(getApplicationContext());
                }).show();
    }

    /** Mantém a operação e o resultado durante a rotação, sem prender a Activity antiga. */
    private static final class Trabalho {
        final Handler principal = new Handler(Looper.getMainLooper());
        RestaurarBackupActivity tela;
        ArquivoBackup copia;
        boolean ocupado;
        String mensagem = "Escolha uma cópia para conferir os dados antes de recuperar.";

        void atualizar() {
            if (tela != null) tela.mostrar();
        }

        void ler(Context contexto, Uri origem) {
            ocupado = true;
            copia = null;
            mensagem = "Conferindo a cópia…";
            atualizar();
            new Thread(() -> {
                ArquivoBackup resultado = null;
                String aviso;
                try {
                    ArquivoBackup lida;
                    try (InputStream entrada = contexto.getContentResolver().openInputStream(origem)) {
                        lida = ArquivoBackup.ler(entrada);
                    }
                    resultado = lida;
                    aviso = "Cópia conferida. Revise os dados abaixo.";
                } catch (IllegalArgumentException e) {
                    aviso = e.getMessage();
                } catch (org.json.JSONException | StackOverflowError e) {
                    aviso = "O arquivo não é uma cópia válida do Mercadinho.";
                } catch (java.io.IOException | SecurityException e) {
                    aviso = "Não foi possível ler o arquivo. Escolha uma cópia acessível neste celular.";
                }
                ArquivoBackup pronta = resultado;
                String texto = aviso;
                principal.post(() -> {
                    copia = pronta;
                    mensagem = texto;
                    ocupado = false;
                    atualizar();
                });
            }, "conferir-backup").start();
        }

        void gravar(Context contexto) {
            ArquivoBackup pronta = copia;
            ocupado = true;
            mensagem = "Recuperando os dados. Aguarde…";
            atualizar();
            new Thread(() -> {
                String aviso;
                try {
                    SharedPreferences prefs = contexto.getSharedPreferences("produtos", MODE_PRIVATE);
                    RestauracaoDados.Resultado resultado = RestauracaoDados.aplicar(new RestauracaoDados.Armazenamento() {
                        public String ler(String chave) { return prefs.getString(chave, null); }
                        public boolean gravar(String produtos, String vendas) {
                            return prefs.edit().putString("lista", produtos).putString("vendas", vendas).commit();
                        }
                    }, pronta.produtos, pronta.vendas);
                    if (resultado == RestauracaoDados.Resultado.SUCESSO) {
                        aviso = "Cópia recuperada. Os produtos, o estoque e as vendas foram atualizados.";
                    } else {
                        aviso = resultado == RestauracaoDados.Resultado.REVERTIDO
                                ? "Não foi possível recuperar a cópia. Os dados anteriores foram mantidos. Confira o espaço no celular."
                                : "A gravação falhou e não foi possível confirmar a recuperação dos dados anteriores. Libere espaço e confira os dados antes de vender.";
                    }
                } catch (RuntimeException e) {
                    aviso = "Não foi possível concluir a recuperação. Confira os dados do aplicativo antes de tentar novamente.";
                }
                String texto = aviso;
                principal.post(() -> {
                    ocupado = false;
                    copia = null;
                    mensagem = texto;
                    atualizar();
                });
            }, "recuperar-backup").start();
        }
    }
}
