package com.mercadinho.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.botao_produtos).setOnClickListener(v ->
                startActivity(new Intent(this, ProdutosActivity.class)));
    }
}
