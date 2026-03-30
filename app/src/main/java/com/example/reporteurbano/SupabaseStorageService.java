package com.example.reporteurbano;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SupabaseStorageService {

    private final OkHttpClient client = new OkHttpClient();
    private final SessionManager sessionManager;

    public SupabaseStorageService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void uploadImage(File file, SupabaseCallback<String> callback) {
        String accessToken = sessionManager.getAccessToken();
        String userId = sessionManager.getUserId();

        if (accessToken == null || userId == null) {
            callback.onError("Sessão expirada. Faça login novamente.");
            return;
        }

        try {
            String fileName = "reporte_" + System.currentTimeMillis() + ".jpg";
            String objectPath = userId + "/" + fileName;
            String encodedPath = URLEncoder.encode(objectPath, StandardCharsets.UTF_8.toString()).replace("+", "%20");

            RequestBody body = RequestBody.create(file, MediaType.get("image/jpeg"));
            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/storage/v1/object/" + SupabaseConfig.STORAGE_BUCKET + "/" + encodedPath)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("x-upsert", "false")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    callback.onError("Erro ao enviar a foto: " + e.getMessage());
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        callback.onError(responseBody.isEmpty() ? "Erro ao enviar a foto." : responseBody);
                        return;
                    }

                    String publicUrl = SupabaseConfig.SUPABASE_URL
                            + "/storage/v1/object/public/"
                            + SupabaseConfig.STORAGE_BUCKET
                            + "/" + objectPath;
                    callback.onSuccess(publicUrl);
                }
            });
        } catch (Exception e) {
            callback.onError("Não foi possível preparar o upload da imagem.");
        }
    }

    public void deleteImageByPublicUrl(String publicUrl, SupabaseCallback<Void> callback) {
        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null) {
            callback.onError("Sessão expirada. Faça login novamente.");
            return;
        }

        try {
            String marker = "/storage/v1/object/public/" + SupabaseConfig.STORAGE_BUCKET + "/";
            int index = publicUrl.indexOf(marker);
            if (index == -1) {
                callback.onError("URL da foto inválida para exclusão.");
                return;
            }

            String objectPath = publicUrl.substring(index + marker.length());
            String encodedPath = URLEncoder.encode(objectPath, StandardCharsets.UTF_8.toString()).replace("+", "%20");

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/storage/v1/object/" + SupabaseConfig.STORAGE_BUCKET + "/" + encodedPath)
                    .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .delete()
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    callback.onError("Erro ao apagar a foto: " + e.getMessage());
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    ResponseBody body = response.body();
                    if (!response.isSuccessful()) {
                        callback.onError(body != null ? body.string() : "Erro ao apagar a foto.");
                        return;
                    }
                    callback.onSuccess(null);
                }
            });
        } catch (Exception e) {
            callback.onError("Não foi possível preparar a exclusão da foto.");
        }
    }
}
