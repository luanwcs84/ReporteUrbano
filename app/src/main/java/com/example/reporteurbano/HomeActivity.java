package com.example.reporteurbano;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private FloatingActionButton btnNovoReporte;
    private MapView map;
    private SessionManager sessionManager;
    private SupabaseReporteService reporteService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            voltarParaLogin();
            return;
        }

        reporteService = new SupabaseReporteService(sessionManager);

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbarHome);
        btnNovoReporte = findViewById(R.id.btnNovoReporte);

        map = findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(12.0);
        GeoPoint startPoint = new GeoPoint(-3.7319, -38.5267);
        map.getController().setCenter(startPoint);

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

        setupUserInterface();

        btnNovoReporte.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NovoReporteActivity.class);
            startActivity(intent);
        });
    }

    private void setupUserInterface() {
        View headerView = navigationView.getHeaderView(0);
        TextView txtLetraAvatarNav = headerView.findViewById(R.id.txtLetraAvatar);
        TextView txtNomeUsuarioNav = headerView.findViewById(R.id.txtNomeUsuarioNav);
        TextView txtTipoContaNav = headerView.findViewById(R.id.txtTipoContaNav);
        TextView txtLetraAvatarToolbar = findViewById(R.id.txtLetraAvatarToolbar);
        View viewAvatarBgNav = headerView.findViewById(R.id.viewAvatarBg);
        View viewAvatarBgToolbar = findViewById(R.id.viewAvatarBgToolbar);

        String nomeUsuario = sessionManager.getUserName();
        if (nomeUsuario == null || nomeUsuario.isEmpty()) {
            nomeUsuario = sessionManager.getUserEmail();
        }
        if (nomeUsuario == null || nomeUsuario.isEmpty()) {
            nomeUsuario = getIntent().getStringExtra("USER_EMAIL");
        }
        if (nomeUsuario == null || nomeUsuario.isEmpty()) {
            nomeUsuario = "Usuário";
        }

        String emailUsuario = sessionManager.getUserEmail();
        if (emailUsuario == null || emailUsuario.isEmpty()) {
            emailUsuario = "usuario@exemplo.com";
        }

        String primeiraLetra = nomeUsuario.substring(0, 1).toUpperCase();
        txtLetraAvatarNav.setText(primeiraLetra);
        txtNomeUsuarioNav.setText(nomeUsuario);
        txtTipoContaNav.setText(sessionManager.isAdmin() ? "Administrador" : emailUsuario);
        txtLetraAvatarToolbar.setText(primeiraLetra);

        Menu menu = navigationView.getMenu();
        menu.findItem(R.id.nav_reportes).setTitle(sessionManager.isAdmin() ? "Todos os Reportes" : "Meus Reportes");

        if (sessionManager.isAdmin()) {
            int adminPrimary = Color.parseColor("#0F766E");
            int adminAccent = Color.parseColor("#F59E0B");
            toolbar.setBackgroundColor(adminPrimary);
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setSubtitle("Modo administrador");
            toolbar.setSubtitleTextColor(Color.WHITE);
            getWindow().setStatusBarColor(adminPrimary);
            headerView.setBackgroundColor(adminPrimary);
            btnNovoReporte.setBackgroundTintList(ColorStateList.valueOf(adminAccent));
            viewAvatarBgNav.setBackgroundTintList(ColorStateList.valueOf(adminAccent));
            viewAvatarBgToolbar.setBackgroundTintList(ColorStateList.valueOf(adminAccent));
        } else {
            int avatarColor = Color.parseColor("#009688");
            toolbar.setSubtitle(null);
            viewAvatarBgNav.setBackgroundTintList(ColorStateList.valueOf(avatarColor));
            viewAvatarBgToolbar.setBackgroundTintList(ColorStateList.valueOf(avatarColor));
        }

        toolbar.setNavigationOnClickListener(v -> drawerLayout.open());

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_reportes) {
                Intent intent = new Intent(HomeActivity.this, MeusReportesActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_sobre) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Sobre nós")
                        .setMessage("O ReporteUrbano ajuda os usuários a registrar problemas da cidade com foto e localização.")
                        .setPositiveButton("OK", null)
                        .show();
            } else if (id == R.id.nav_sair) {
                sessionManager.clearSession();
                voltarParaLogin();
            }
            drawerLayout.close();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        atualizarMapaComReportes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    private void atualizarMapaComReportes() {
        LinearLayout layoutEstadoVazio = findViewById(R.id.layoutEstadoVazio);

        reporteService.getVisibleReportes(new SupabaseCallback<List<Reporte>>() {
            @Override
            public void onSuccess(List<Reporte> listaReportes) {
                runOnUiThread(() -> {
                    map.getOverlays().clear();

                    if (listaReportes.isEmpty()) {
                        map.setVisibility(View.GONE);
                        layoutEstadoVazio.setVisibility(View.VISIBLE);
                    } else {
                        map.setVisibility(View.VISIBLE);
                        layoutEstadoVazio.setVisibility(View.GONE);

                        for (Reporte reporte : listaReportes) {
                            Marker marker = new Marker(map);
                            marker.setPosition(new GeoPoint(reporte.getLatitude(), reporte.getLongitude()));
                            marker.setTitle(reporte.getTitulo());
                            marker.setSnippet(
                                    "Autor: " + reporte.getAutorNome()
                                            + "\n"
                                            + "Endereço: " + reporte.getEndereco()
                                            + "\n"
                                            + "Descrição: " + reporte.getDescricao()
                            );
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            map.getOverlays().add(marker);
                        }

                        Reporte primeiroReporte = listaReportes.get(0);
                        map.getController().animateTo(new GeoPoint(primeiroReporte.getLatitude(), primeiroReporte.getLongitude()));
                    }
                    map.invalidate();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(HomeActivity.this, errorMessage, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void voltarParaLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
