package com.example.reporteurbano;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SupabaseAuthService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final SessionManager sessionManager;

    public SupabaseAuthService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void signUp(String email, String password, SupabaseCallback<AuthUser> callback) {
        if (!SupabaseConfig.isConfigured()) {
            callback.onError("Configure a URL e a ANON KEY do Supabase em SupabaseConfig.java.");
            return;
        }

        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("email", email);
            bodyJson.put("password", password);

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/auth/v1/signup")
                    .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(bodyJson.toString(), JSON))
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    callback.onError("Erro de rede no cadastro: " + e.getMessage());
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        callback.onError(parseSupabaseError(responseBody, "Erro ao cadastrar usuário."));
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        JSONObject user = json.optJSONObject("user");
                        JSONObject session = json.optJSONObject("session");

                        if (user == null) {
                            callback.onError("Cadastro realizado, mas a resposta do usuário veio vazia.");
                            return;
                        }

                        String userId = user.optString("id", "");
                        String userEmail = user.optString("email", email);

                        if (session != null) {
                            sessionManager.saveSession(
                                    session.optString("access_token", null),
                                    session.optString("refresh_token", null),
                                    userId,
                                    userEmail
                            );
                        }

                        callback.onSuccess(new AuthUser(userId, userEmail));
                    } catch (Exception e) {
                        callback.onError("Não foi possível interpretar a resposta do cadastro.");
                    }
                }
            });
        } catch (Exception e) {
            callback.onError("Não foi possível montar a requisição de cadastro.");
        }
    }

    public void signIn(String email, String password, SupabaseCallback<AuthUser> callback) {
        if (!SupabaseConfig.isConfigured()) {
            callback.onError("Configure a URL e a ANON KEY do Supabase em SupabaseConfig.java.");
            return;
        }

        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("email", email);
            bodyJson.put("password", password);

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/auth/v1/token?grant_type=password")
                    .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(bodyJson.toString(), JSON))
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    callback.onError("Erro de rede no login: " + e.getMessage());
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        callback.onError(parseSupabaseError(responseBody, "E-mail ou senha inválidos."));
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        JSONObject user = json.getJSONObject("user");

                        String accessToken = json.optString("access_token", null);
                        String refreshToken = json.optString("refresh_token", null);
                        String userId = user.optString("id", "");
                        String userEmail = user.optString("email", email);

                        sessionManager.saveSession(accessToken, refreshToken, userId, userEmail);
                        callback.onSuccess(new AuthUser(userId, userEmail));
                    } catch (Exception e) {
                        callback.onError("Não foi possível interpretar a resposta do login.");
                    }
                }
            });
        } catch (Exception e) {
            callback.onError("Não foi possível montar a requisição de login.");
        }
    }

    public void signOut() {
        sessionManager.clearSession();
    }

    private String parseSupabaseError(String responseBody, String fallback) {
        try {
            JSONObject json = new JSONObject(responseBody);
            String message = json.optString("msg");
            if (message.isEmpty()) {
                message = json.optString("message");
            }
            if (message.isEmpty()) {
                message = json.optString("error_description");
            }
            return message.isEmpty() ? fallback : message;
        } catch (Exception e) {
            return fallback;
        }
    }
}
