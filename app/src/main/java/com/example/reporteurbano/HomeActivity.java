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
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private FloatingActionButton btnNovoReporte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbarHome);
        btnNovoReporte = findViewById(R.id.btnNovoReporte);

        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        View headerView = navigationView.getHeaderView(0);
        TextView txtLetraAvatarNav = headerView.findViewById(R.id.txtLetraAvatar);
        TextView txtNomeUsuarioNav = headerView.findViewById(R.id.txtNomeUsuarioNav);
        TextView txtLetraAvatarToolbar = findViewById(R.id.txtLetraAvatarToolbar);

        String nomeUsuario = "admin";

        if (nomeUsuario != null && !nomeUsuario.isEmpty()) {
            String primeiraLetra = nomeUsuario.substring(0, 1).toUpperCase();
            txtLetraAvatarNav.setText(primeiraLetra);
            txtNomeUsuarioNav.setText(nomeUsuario);
            txtLetraAvatarToolbar.setText(primeiraLetra);
        }

        toolbar.setNavigationOnClickListener(v -> drawerLayout.open());

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_sair) {
                finish();
            }
            drawerLayout.close();
            return true;
        });

        btnNovoReporte.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NovoReporteActivity.class);
            startActivity(intent);
        });
    }
}