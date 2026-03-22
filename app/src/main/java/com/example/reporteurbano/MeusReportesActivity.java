package com.example.reporteurbano;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class MeusReportesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meus_reportes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbarMeusReportes);
        toolbar.setNavigationOnClickListener(v -> finish());

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        List<Reporte> listaDeReportes = dbHelper.buscarTodosReportes();

        RecyclerView recycler = findViewById(R.id.recyclerMeusReportes);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        ReporteAdapter adapter = new ReporteAdapter(listaDeReportes);
        recycler.setAdapter(adapter);
    }
}