package com.example.reporteurbano;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class MeusReportesActivity extends AppCompatActivity {

    private ReporteAdapter adapter;
    private SupabaseReporteService reporteService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meus_reportes);

        reporteService = new SupabaseReporteService(new SessionManager(this));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbarMeusReportes);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recycler = findViewById(R.id.recyclerMeusReportes);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReporteAdapter(this, new ArrayList<>());
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarReportes();
    }

    private void carregarReportes() {
        reporteService.getMyReportes(new SupabaseCallback<List<Reporte>>() {
            @Override
            public void onSuccess(List<Reporte> result) {
                runOnUiThread(() -> adapter.updateData(result));
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> Toast.makeText(MeusReportesActivity.this, errorMessage, Toast.LENGTH_LONG).show());
            }
        });
    }
}
