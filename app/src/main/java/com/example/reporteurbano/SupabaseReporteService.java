package com.example.reporteurbano;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public void getVisibleReportes(SupabaseCallback<List<Reporte>> callback) {
        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null) {
            callback.onError("Sessao expirada. Faca login novamente.");
            return;
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse(SupabaseConfig.SUPABASE_URL + "/rest/v1/reportes")
                .newBuilder()
                .addQueryParameter("select", "*")
                .addQueryParameter("order", "created_at.desc");

        if (!sessionManager.isAdmin()) {
            urlBuilder.addQueryParameter("user_id", "eq." + sessionManager.getUserId());
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
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
                    List<JSONObject> rawReportes = new ArrayList<>();
                    Set<String> userIds = new HashSet<>();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.getJSONObject(i);
                        rawReportes.add(item);
                        String userId = item.optString("user_id");
                        if (!userId.isEmpty()) {
                            userIds.add(userId);
                        }
                    }

                    Map<String, AutorInfo> autores = buscarAutores(accessToken, userIds);
                    List<Reporte> reportes = new ArrayList<>();

                    for (JSONObject item : rawReportes) {
                        String userId = item.optString("user_id");
                        AutorInfo autor = autores.getOrDefault(userId, AutorInfo.desconhecido());
                        reportes.add(new Reporte(
                                item.optString("id"),
                                userId,
                                item.optString("titulo"),
                                item.optString("descricao"),
                                item.optString("endereco"),
                                item.optDouble("latitude"),
                                item.optDouble("longitude"),
                                item.optString("foto_url"),
                                autor.getNome(),
                                autor.getEmail(),
                                item.optString("created_at")
                        ));
                    }

                    callback.onSuccess(reportes);
                } catch (Exception e) {
                    callback.onError("Nao foi possivel interpretar a lista de reportes.");
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
            callback.onError("Sessao expirada. Faca login novamente.");
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
            callback.onError("Nao foi possivel montar o reporte para envio.");
        }
    }

    public void deleteReporte(String reporteId, SupabaseCallback<Void> callback) {
        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null) {
            callback.onError("Sessao expirada. Faca login novamente.");
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

    private Map<String, AutorInfo> buscarAutores(String accessToken, Set<String> userIds) throws IOException {
        Map<String, AutorInfo> autores = new HashMap<>();
        if (userIds.isEmpty()) {
            return autores;
        }

        StringBuilder ids = new StringBuilder();
        for (String userId : userIds) {
            if (ids.length() > 0) {
                ids.append(",");
            }
            ids.append(userId);
        }

        HttpUrl url = HttpUrl.parse(SupabaseConfig.SUPABASE_URL + "/rest/v1/profiles")
                .newBuilder()
                .addQueryParameter("select", "id,email,nome")
                .addQueryParameter("id", "in.(" + ids + ")")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                return autores;
            }

            JSONArray profiles = new JSONArray(responseBody);
            for (int i = 0; i < profiles.length(); i++) {
                JSONObject profile = profiles.getJSONObject(i);
                String email = profile.optString("email", "");
                String nome = profile.optString("nome");
                if (nome == null || nome.isEmpty()) {
                    nome = extrairNomeDoEmail(email);
                }
                autores.put(profile.optString("id"), new AutorInfo(nome, email));
            }
        } catch (Exception ignored) {
        }

        return autores;
    }

    private String extrairNomeDoEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "Usuario sem identificacao";
        }

        String nomeBase = email.split("@")[0]
                .replace('.', ' ')
                .replace('_', ' ')
                .replace('-', ' ')
                .trim();

        if (nomeBase.isEmpty()) {
            return "Usuario sem identificacao";
        }

        String[] partes = nomeBase.split("\\s+");
        StringBuilder nomeFormatado = new StringBuilder();
        for (String parte : partes) {
            if (parte.isEmpty()) {
                continue;
            }
            if (nomeFormatado.length() > 0) {
                nomeFormatado.append(' ');
            }
            nomeFormatado.append(Character.toUpperCase(parte.charAt(0)));
            if (parte.length() > 1) {
                nomeFormatado.append(parte.substring(1).toLowerCase());
            }
        }

        return nomeFormatado.length() > 0 ? nomeFormatado.toString() : "Usuario sem identificacao";
    }

    private static class AutorInfo {
        private final String nome;
        private final String email;

        private AutorInfo(String nome, String email) {
            this.nome = nome;
            this.email = email;
        }

        public String getNome() {
            return nome;
        }

        public String getEmail() {
            return email == null || email.isEmpty() ? "E-mail nao informado" : email;
        }

        public static AutorInfo desconhecido() {
            return new AutorInfo("Usuario sem identificacao", "E-mail nao informado");
        }
    }
}
