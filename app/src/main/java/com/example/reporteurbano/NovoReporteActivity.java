package com.example.reporteurbano;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Locale;

public class NovoReporteActivity extends AppCompatActivity {

    private ImageView imgFotoReporte;
    private FusedLocationProviderClient fusedLocationClient;
    private TextInputEditText editLocal;
    private Bitmap fotoCapturadaBitmap = null;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    fotoCapturadaBitmap = (Bitmap) extras.get("data");
                    imgFotoReporte.setImageBitmap(fotoCapturadaBitmap);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_novo_reporte);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgFotoReporte = findViewById(R.id.imgFotoReporte);
        editLocal = findViewById(R.id.editLocalReporte);
        TextInputLayout layoutLocal = findViewById(R.id.layoutLocalReporte);
        MaterialButton btnTirarFoto = findViewById(R.id.btnTirarFoto);
        MaterialToolbar toolbar = findViewById(R.id.toolbarNovoReporte);
        toolbar.setNavigationOnClickListener(v -> finish());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnTirarFoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                cameraLauncher.launch(takePictureIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Erro: Câmera não encontrada.", Toast.LENGTH_SHORT).show();
            }
        });
        layoutLocal.setStartIconOnClickListener(v -> obterLocalizacao());

        imgFotoReporte.setOnClickListener(v -> {
            if (fotoCapturadaBitmap != null) {
                android.app.Dialog dialogZoom = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                dialogZoom.setContentView(R.layout.dialog_zoom_imagem);

                com.github.chrisbanes.photoview.PhotoView photoViewZoom = dialogZoom.findViewById(R.id.photoViewZoom);
                photoViewZoom.setImageBitmap(fotoCapturadaBitmap);

                dialogZoom.show();
            }
        });
    }

    private void obterLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        preencherEndereco(location);
                    } else {
                        Toast.makeText(this, "Ajuste a localização no menu do emulador.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void preencherEndereco(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                editLocal.setText(addresses.get(0).getAddressLine(0));
            }
        } catch (Exception e) {
            editLocal.setText(location.getLatitude() + ", " + location.getLongitude());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obterLocalizacao();
        }
    }
}