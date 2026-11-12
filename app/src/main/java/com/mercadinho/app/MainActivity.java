package com.mercadinho.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.botao_vender).setOnClickListener(v ->
                startActivity(new Intent(this, VenderActivity.class)));
        findViewById(R.id.botao_produtos).setOnClickListener(v ->
                startActivity(new Intent(this, ProdutosActivity.class)));
    }
}
