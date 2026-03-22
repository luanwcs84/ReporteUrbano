package com.example.reporteurbano;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class NovoReporteActivity extends AppCompatActivity {

    private ImageView imgFotoReporte;
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Pegamos na miniatura (thumbnail) da foto que a câmera devolveu
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");

                    // Colocamos essa foto no nosso ImageView (o quadrado cinza)
                    imgFotoReporte.setImageBitmap(imageBitmap);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_novo_reporte);

        imgFotoReporte = findViewById(R.id.imgFotoReporte);
        MaterialButton btnTirarFoto = findViewById(R.id.btnTirarFoto);
        MaterialToolbar toolbar = findViewById(R.id.toolbarNovoReporte);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnTirarFoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            try {
                cameraLauncher.launch(takePictureIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Erro: Câmera não encontrada.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}