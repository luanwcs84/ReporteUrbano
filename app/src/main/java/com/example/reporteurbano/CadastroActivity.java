package com.example.reporteurbano;

import android.os.Bundle;
import android.view.View;
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

public class CadastroActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private SupabaseAuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        sessionManager = new SessionManager(this);
        authService = new SupabaseAuthService(sessionManager);

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

        btnVoltar.setOnClickListener(v -> finish());

        btnFinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usuario = editUsuario.getText() != null ? editUsuario.getText().toString().trim() : "";
                String email = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";
                String senha = editSenha.getText() != null ? editSenha.getText().toString().trim() : "";
                String confirmar = editConfirmar.getText() != null ? editConfirmar.getText().toString().trim() : "";

                layoutUsuario.setError(null);
                layoutEmail.setError(null);
                layoutSenha.setError(null);
                layoutConfirmar.setError(null);

                if (usuario.isEmpty()) {
                    layoutUsuario.setError("Preencha o nome do usuario");
                    return;
                }

                if (email.isEmpty()) {
                    layoutEmail.setError("Preencha o e-mail");
                    return;
                }

                if (senha.length() < 6) {
                    layoutSenha.setError("A senha deve ter pelo menos 6 caracteres");
                    return;
                }

                if (!senha.equals(confirmar)) {
                    layoutConfirmar.setError("As senhas nao coincidem");
                    return;
                }

                final android.app.AlertDialog loadingDialog =
                        LoadingUtils.createLoadingDialog(CadastroActivity.this, "Criando conta...");

                authService.signUp(email, senha, new SupabaseCallback<AuthUser>() {
                    @Override
                    public void onSuccess(AuthUser result) {
                        runOnUiThread(() -> {
                            loadingDialog.dismiss();
                            Toast.makeText(CadastroActivity.this, "Conta criada com sucesso. Faca login para continuar.", Toast.LENGTH_LONG).show();
                            sessionManager.clearSession();
                            finish();
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            loadingDialog.dismiss();
                            Toast.makeText(CadastroActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
        });
    }
}
