package com.example.reporteurbano;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseReporteService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final SessionManager sessionManager;

    public SupabaseReporteService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void getMyReportes(SupabaseCallback<List<Reporte>> callback) {
        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null) {
            callback.onError("Sessão expirada. Faça login novamente.");
            return;
        }

        HttpUrl url = HttpUrl.parse(SupabaseConfig.SUPABASE_URL + "/rest/v1/reportes")
                .newBuilder()
                .addQueryParameter("select", "*")
                .addQueryParameter("order", "created_at.desc")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callback.onError("Erro ao buscar reportes: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    callback.onError(responseBody.isEmpty() ? "Erro ao buscar reportes." : responseBody);
                    return;
                }

                try {
                    JSONArray array = new JSONArray(responseBody);
                    List<Reporte> reportes = new ArrayList<>();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.getJSONObject(i);
                        reportes.add(new Reporte(
                                item.optString("id"),
                                item.optString("titulo"),
                                item.optString("descricao"),
                                item.optString("endereco"),
                                item.optDouble("latitude"),
                                item.optDouble("longitude"),
                                item.optString("foto_url")
                        ));
                    }

                    callback.onSuccess(reportes);
                } catch (Exception e) {
                    callback.onError("Não foi possível interpretar a lista de reportes.");
                }
            }
        });
    }

    public void createReporte(String titulo,
                              String descricao,
                              String endereco,
                              double latitude,
                              double longitude,
                              String fotoUrl,
                              SupabaseCallback<Void> callback) {
        String accessToken = sessionManager.getAccessToken();
        String userId = sessionManager.getUserId();

        if (accessToken == null || userId == null) {
            callback.onError("Sessão expirada. Faça login novamente.");
            return;
        }

        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("user_id", userId);
            bodyJson.put("titulo", titulo);
            bodyJson.put("descricao", descricao);
            bodyJson.put("endereco", endereco);
            bodyJson.put("latitude", latitude);
            bodyJson.put("longitude", longitude);
            bodyJson.put("foto_url", fotoUrl);

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/reportes")
                    .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .post(RequestBody.create(bodyJson.toString(), JSON))
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    callback.onError("Erro ao salvar reporte: " + e.getMessage());
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        callback.onError(responseBody.isEmpty() ? "Erro ao salvar reporte." : responseBody);
                        return;
                    }
                    callback.onSuccess(null);
                }
            });
        } catch (Exception e) {
            callback.onError("Não foi possível montar o reporte para envio.");
        }
    }

    public void deleteReporte(String reporteId, SupabaseCallback<Void> callback) {
        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null) {
            callback.onError("Sessão expirada. Faça login novamente.");
            return;
        }

        HttpUrl url = HttpUrl.parse(SupabaseConfig.SUPABASE_URL + "/rest/v1/reportes")
                .newBuilder()
                .addQueryParameter("id", "eq." + reporteId)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .delete()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callback.onError("Erro ao apagar reporte: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    callback.onError(responseBody.isEmpty() ? "Erro ao apagar reporte." : responseBody);
                    return;
                }
                callback.onSuccess(null);
            }
        });
    }
}
