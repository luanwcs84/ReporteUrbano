package com.example.reporteurbano;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseProfileService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final SessionManager sessionManager;

    public SupabaseProfileService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void createDefaultProfile(String userId, String email, String nome, SupabaseCallback<ProfileData> callback) {
        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null) {
            callback.onError("Sessao expirada. Faca login novamente.");
            return;
        }

        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("id", userId);
            bodyJson.put("email", email);
            bodyJson.put("nome", nome);
            bodyJson.put("role", "user");

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/profiles")
                    .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                    .post(RequestBody.create(bodyJson.toString(), JSON))
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    callback.onError("Erro ao criar perfil: " + e.getMessage());
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        callback.onError(responseBody.isEmpty() ? "Erro ao criar perfil." : responseBody);
                        return;
                    }
                    callback.onSuccess(new ProfileData(nome, "user"));
                }
            });
        } catch (Exception e) {
            callback.onError("Nao foi possivel montar a requisicao do perfil.");
        }
    }

    public void fetchCurrentUserProfile(SupabaseCallback<ProfileData> callback) {
        String accessToken = sessionManager.getAccessToken();
        String userId = sessionManager.getUserId();

        if (accessToken == null || userId == null) {
            callback.onError("Sessao expirada. Faca login novamente.");
            return;
        }

        HttpUrl url = HttpUrl.parse(SupabaseConfig.SUPABASE_URL + "/rest/v1/profiles")
                .newBuilder()
                .addQueryParameter("select", "role,nome,email")
                .addQueryParameter("id", "eq." + userId)
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
                callback.onError("Erro ao buscar perfil: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    callback.onError(responseBody.isEmpty() ? "Erro ao buscar perfil." : responseBody);
                    return;
                }

                try {
                    JSONArray array = new JSONArray(responseBody);
                    if (array.length() == 0) {
                        callback.onSuccess(new ProfileData(null, "user"));
                        return;
                    }

                    JSONObject item = array.getJSONObject(0);
                    String nome = item.optString("nome");
                    if (nome == null || nome.isEmpty()) {
                        nome = item.optString("email", null);
                    }
                    callback.onSuccess(new ProfileData(nome, item.optString("role", "user")));
                } catch (Exception e) {
                    callback.onError("Nao foi possivel interpretar a role do usuario.");
                }
            }
        });
    }

    public static class ProfileData {
        private final String nome;
        private final String role;

        public ProfileData(String nome, String role) {
            this.nome = nome;
            this.role = role;
        }

        public String getNome() {
            return nome;
        }

        public String getRole() {
            return role;
        }
    }
}
