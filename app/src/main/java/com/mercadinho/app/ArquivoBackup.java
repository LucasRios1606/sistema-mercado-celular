package com.mercadinho.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

final class ArquivoBackup {
    final String produtos;
    final String vendas;
    final int quantidadeProdutos;
    final int quantidadeVendas;
    final long criadoEm;

    private ArquivoBackup(JSONObject copia) throws JSONException {
        JSONArray listaProdutos = copia.getJSONArray("produtos");
        JSONArray listaVendas = copia.getJSONArray("vendas");
        produtos = listaProdutos.toString();
        vendas = listaVendas.toString();
        quantidadeProdutos = listaProdutos.length();
        quantidadeVendas = listaVendas.length();
        criadoEm = copia.getLong("criadoEm");
    }

    static ArquivoBackup ler(InputStream entrada) throws IOException, JSONException {
        if (entrada == null) throw new IOException("Arquivo indisponível");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] bloco = new byte[8192];
        int lidos;
        while ((lidos = entrada.read(bloco)) != -1) {
            if (bytes.size() + lidos > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("Escolha uma cópia de até 10 MB.");
            }
            bytes.write(bloco, 0, lidos);
        }
        String texto = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes.toByteArray())).toString();
        JSONTokener leitor = new JSONTokener(texto);
        Object raiz = leitor.nextValue();
        if (!(raiz instanceof JSONObject) || leitor.nextClean() != 0) {
            throw new JSONException("Conteúdo inválido");
        }
        JSONObject copia = (JSONObject) raiz;
        ValidacaoBackup.conferir((Map<?, ?>) converter(copia, 0));
        return new ArquivoBackup(copia);
    }

    private static Object converter(Object valor, int nivel) throws JSONException {
        if (nivel > 12) throw new JSONException("Estrutura inválida");
        if (valor instanceof JSONObject) {
            JSONObject objeto = (JSONObject) valor;
            Map<String, Object> campos = new HashMap<>();
            Iterator<String> chaves = objeto.keys();
            while (chaves.hasNext()) {
                String chave = chaves.next();
                campos.put(chave, converter(objeto.get(chave), nivel + 1));
            }
            return campos;
        }
        if (valor instanceof JSONArray) {
            JSONArray lista = (JSONArray) valor;
            ArrayList<Object> itens = new ArrayList<>();
            for (int i = 0; i < lista.length(); i++) itens.add(converter(lista.get(i), nivel + 1));
            return itens;
        }
        return valor;
    }
}
