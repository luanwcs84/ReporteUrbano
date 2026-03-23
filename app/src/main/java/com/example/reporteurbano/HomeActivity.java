package com.example.reporteurbano;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        map.getController().setZoom(15.0);
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
        TextView txtLetraAvatarToolbar = findViewById(R.id.txtLetraAvatarToolbar);

        String emailUsuario = getIntent().getStringExtra("USER_EMAIL");
        if (emailUsuario == null || emailUsuario.isEmpty()) {
            emailUsuario = "admin@gmail.com";
        }

        String primeiraLetra = emailUsuario.substring(0, 1).toUpperCase();
        txtLetraAvatarNav.setText(primeiraLetra);
        txtNomeUsuarioNav.setText(emailUsuario);
        txtLetraAvatarToolbar.setText(primeiraLetra);

        toolbar.setNavigationOnClickListener(v -> drawerLayout.open());

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_reportes) {
                Intent intent = new Intent(HomeActivity.this, MeusReportesActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_sair) {
                finish();
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
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Reporte> listaReportes = dbHelper.buscarTodosReportes();

        map.getOverlays().clear();

        if (listaReportes.isEmpty()) {
            map.setVisibility(View.GONE);
            layoutEstadoVazio.setVisibility(View.VISIBLE);
        } else {
            map.setVisibility(View.VISIBLE);
            layoutEstadoVazio.setVisibility(View.GONE);

            for (Reporte reporte : listaReportes) {
                try {
                    String[] coordenadas = reporte.getLocal().split(",");
                    double lat = Double.parseDouble(coordenadas[0].trim());
                    double lon = Double.parseDouble(coordenadas[1].trim());

                    Marker marker = new Marker(map);
                    marker.setPosition(new GeoPoint(lat, lon));
                    marker.setTitle(reporte.getTitulo());
                    marker.setSnippet(reporte.getDescricao());
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                    map.getOverlays().add(marker);
                } catch (Exception e) {
                }
            }
        }
        map.invalidate();
    }
}