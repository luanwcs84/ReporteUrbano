package com.example.reporteurbano;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CadastroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextInputLayout layoutUsuario = findViewById(R.id.layoutUsuarioCad);
        TextInputLayout layoutEmail = findViewById(R.id.layoutEmailCad);
        TextInputLayout layoutSenha = findViewById(R.id.layoutSenhaCad);
        TextInputLayout layoutConfirmar = findViewById(R.id.layoutConfirmarSenhaCad);

        TextInputEditText editUsuario = findViewById(R.id.editUsuarioCad);
        TextInputEditText editEmail = findViewById(R.id.editEmailCad);
        TextInputEditText editSenha = findViewById(R.id.editSenhaCad);
        TextInputEditText editConfirmar = findViewById(R.id.editConfirmarSenhaCad);

        MaterialButton btnFinalizar = findViewById(R.id.btnFinalizarCadastro);
        TextView btnVoltar = findViewById(R.id.txtVoltarLogin);

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lógica de cadastro futura
            }
        });
    }
}