package com.example.reporteurbano;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private SupabaseAuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        authService = new SupabaseAuthService(sessionManager);

        if (sessionManager.isLoggedIn()) {
            abrirHome(sessionManager.getUserEmail());
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });

        final TextInputLayout layoutEmail = findViewById(R.id.layoutEmail);
        final TextInputLayout layoutSenha = findViewById(R.id.layoutSenha);
        final TextInputEditText editEmail = findViewById(R.id.editEmail);
        final TextInputEditText editSenha = findViewById(R.id.editSenha);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        TextView btnIrParaCadastro = findViewById(R.id.txtCadastrar);
        final ScrollView scrollRoot = findViewById(R.id.main);

        layoutEmail.setErrorIconDrawable(null);
        layoutSenha.setErrorIconDrawable(null);
        layoutSenha.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

        btnIrParaCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";
            String senha = editSenha.getText() != null ? editSenha.getText().toString().trim() : "";

            layoutEmail.setError(null);
            layoutSenha.setError(null);
            layoutSenha.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

            if (email.isEmpty()) {
                layoutEmail.setError("Preencha o e-mail");
                focarCampoComTeclado(scrollRoot, editEmail);
                return;
            }

            if (senha.isEmpty()) {
                layoutSenha.setError("Preencha a senha");
                focarCampoComTeclado(scrollRoot, editSenha);
                return;
            }

            final android.app.AlertDialog loadingDialog =
                    LoadingUtils.createLoadingDialog(MainActivity.this, "Entrando...");

            authService.signIn(email, senha, new SupabaseCallback<AuthUser>() {
                @Override
                public void onSuccess(AuthUser result) {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        abrirHome(result.getEmail());
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        layoutSenha.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

                        if ("Nenhuma conta encontrada para este e-mail.".equals(errorMessage)) {
                            layoutEmail.setError(errorMessage);
                            layoutSenha.setError(null);
                            focarCampoComTeclado(scrollRoot, editEmail);
                        } else if ("Senha incorreta. Tente novamente.".equals(errorMessage)) {
                            layoutEmail.setError(null);
                            layoutSenha.setError(errorMessage);
                            focarCampoComTeclado(scrollRoot, editSenha);
                        } else {
                            layoutEmail.setError(" ");
                            layoutSenha.setError(errorMessage);
                            focarCampoComTeclado(scrollRoot, editSenha);
                        }

                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }

    private void abrirHome(String email) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
        finish();
    }

    private void focarCampoComTeclado(ScrollView scrollRoot, View campo) {
        campo.requestFocus();
        scrollRoot.post(() -> scrollRoot.smoothScrollTo(0, Math.max(0, campo.getTop() - 120)));
    }
}
