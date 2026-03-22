package com.example.reporteurbano;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        final TextInputLayout layoutEmail = findViewById(R.id.layoutEmail);
        final TextInputLayout layoutSenha = findViewById(R.id.layoutSenha);
        final TextInputEditText editEmail = findViewById(R.id.editEmail);
        final TextInputEditText editSenha = findViewById(R.id.editSenha);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        TextView btnIrParaCadastro = findViewById(R.id.txtCadastrar);

        btnIrParaCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CadastroActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString().trim();
                String senha = editSenha.getText().toString().trim();

                layoutEmail.setError(null);
                layoutSenha.setError(null);

                if (email.isEmpty()) {
                    layoutEmail.setError("Preencha o e-mail");
                } else if (senha.isEmpty()) {
                    layoutSenha.setError("Preencha a senha");
                } else {
                    if (email.equals("admin@gmail.com") && senha.equals("1234")) {
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.putExtra("USER_EMAIL", email);
                        startActivity(intent);
                        finish();
                    } else {
                        layoutEmail.setError(" ");
                        layoutSenha.setError("E-mail ou senha incorretos");
                    }
                }
            }
        });
    }
}