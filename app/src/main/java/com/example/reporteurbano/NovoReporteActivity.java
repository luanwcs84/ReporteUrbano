package com.example.reporteurbano;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

public class NovoReporteActivity extends AppCompatActivity {

    private ImageView imgFotoReporte;
    private FusedLocationProviderClient fusedLocationClient;
    private TextInputEditText editTitulo;
    private TextInputEditText editDescricao;
    private TextInputEditText editLocal;
    private Bitmap fotoCapturadaBitmap = null;
    private SessionManager sessionManager;
    private SupabaseReporteService reporteService;
    private SupabaseStorageService storageService;

    private final ActivityResultLauncher<android.content.Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        fotoCapturadaBitmap = (Bitmap) extras.get("data");
                        imgFotoReporte.setImageBitmap(fotoCapturadaBitmap);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_novo_reporte);

        sessionManager = new SessionManager(this);
        reporteService = new SupabaseReporteService(sessionManager);
        storageService = new SupabaseStorageService(sessionManager);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgFotoReporte = findViewById(R.id.imgFotoReporte);
        editTitulo = findViewById(R.id.editTituloReporte);
        editDescricao = findViewById(R.id.editDescricaoReporte);
        editLocal = findViewById(R.id.editLocalReporte);

        TextInputLayout layoutLocal = findViewById(R.id.layoutLocalReporte);
        MaterialButton btnTirarFoto = findViewById(R.id.btnTirarFoto);
        MaterialButton btnEnviarReporte = findViewById(R.id.btnEnviarReporte);
        MaterialToolbar toolbar = findViewById(R.id.toolbarNovoReporte);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        toolbar.setNavigationOnClickListener(v -> finish());

        btnTirarFoto.setOnClickListener(v -> {
            android.content.Intent takePictureIntent = new android.content.Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                cameraLauncher.launch(takePictureIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Camera nao encontrada.", Toast.LENGTH_SHORT).show();
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

        btnEnviarReporte.setOnClickListener(v -> salvarReporteCompleto());
    }

    private void salvarReporteCompleto() {
        String titulo = editTitulo.getText() != null ? editTitulo.getText().toString().trim() : "";
        String descricao = editDescricao.getText() != null ? editDescricao.getText().toString().trim() : "";
        String endereco = editLocal.getText() != null ? editLocal.getText().toString().trim() : "";

        if (titulo.isEmpty() || descricao.isEmpty() || endereco.isEmpty() || fotoCapturadaBitmap == null) {
            Toast.makeText(this, "Preencha todos os campos e adicione uma foto.", Toast.LENGTH_SHORT).show();
            return;
        }

        final android.app.AlertDialog loadingDialog =
                LoadingUtils.createLoadingDialog(this, "Enviando reporte...");

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(endereco, 1);
                if (addresses == null || addresses.isEmpty()) {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "Nao foi possivel localizar esse endereco.", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                Address local = addresses.get(0);
                double latitude = local.getLatitude();
                double longitude = local.getLongitude();

                File fotoTemporaria = salvarImagemTemporaria(fotoCapturadaBitmap);
                if (fotoTemporaria == null) {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "Erro ao preparar a foto.", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                storageService.uploadImage(fotoTemporaria, new SupabaseCallback<String>() {
                    @Override
                    public void onSuccess(String fotoUrl) {
                        reporteService.createReporte(titulo, descricao, endereco, latitude, longitude, fotoUrl, new SupabaseCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                runOnUiThread(() -> {
                                    loadingDialog.dismiss();
                                    Toast.makeText(NovoReporteActivity.this, "Reporte salvo com sucesso.", Toast.LENGTH_LONG).show();
                                    finish();
                                });
                            }

                            @Override
                            public void onError(String errorMessage) {
                                runOnUiThread(() -> {
                                    loadingDialog.dismiss();
                                    Toast.makeText(NovoReporteActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            loadingDialog.dismiss();
                            Toast.makeText(NovoReporteActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Erro ao processar o endereco informado.", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private File salvarImagemTemporaria(Bitmap bitmap) {
        try {
            File arquivoImagem = new File(getCacheDir(), "reporte_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(arquivoImagem);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            return arquivoImagem;
        } catch (Exception e) {
            return null;
        }
    }

    private void obterLocalizacao() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                editLocal.setText(addresses.get(0).getAddressLine(0));
                            } else {
                                editLocal.setText(location.getLatitude() + ", " + location.getLongitude());
                            }
                        } catch (Exception e) {
                            editLocal.setText(location.getLatitude() + ", " + location.getLongitude());
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obterLocalizacao();
        }
    }
}
