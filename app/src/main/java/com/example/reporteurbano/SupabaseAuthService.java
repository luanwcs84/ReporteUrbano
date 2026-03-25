package com.example.reporteurbano;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseAuthService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final SessionManager sessionManager;
    private final SupabaseProfileService profileService;

    public SupabaseAuthService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.profileService = new SupabaseProfileService(sessionManager);
    }

    public void signUp(String nome, String email, String password, SupabaseCallback<AuthUser> callback) {
        if (!SupabaseConfig.isConfigured()) {
            callback.onError("Configure a URL e a chave publica do Supabase em SupabaseConfig.java.");
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
                        callback.onError(parseSupabaseError(responseBody, "Erro ao cadastrar usuario."));
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        JSONObject user = json.optJSONObject("user");
                        JSONObject session = json.optJSONObject("session");

                        if (user == null) {
                            callback.onError("Nao foi possivel interpretar a resposta do cadastro.");
                            return;
                        }

                        String userId = user.optString("id", "");
                        String userEmail = user.optString("email", email);

                        // Quando a confirmacao de e-mail esta ativa no Supabase, o cadastro pode
                        // ser criado com sucesso sem retornar uma sessao imediatamente.
                        if (session == null) {
                            callback.onSuccess(new AuthUser(userId, userEmail, nome));
                            return;
                        }

                        String accessToken = session.optString("access_token", null);
                        String refreshToken = session.optString("refresh_token", null);

                        sessionManager.saveSession(accessToken, refreshToken, userId, userEmail, "user", nome);
                        callback.onSuccess(new AuthUser(userId, userEmail, nome));
                    } catch (Exception e) {
                        callback.onError("Nao foi possivel interpretar a resposta do cadastro.");
                    }
                }
            });
        } catch (Exception e) {
            callback.onError("Nao foi possivel montar a requisicao de cadastro.");
        }
    }

    public void signIn(String email, String password, SupabaseCallback<AuthUser> callback) {
        if (!SupabaseConfig.isConfigured()) {
            callback.onError("Configure a URL e a chave publica do Supabase em SupabaseConfig.java.");
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
                        tratarErroLogin(email, responseBody, callback);
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        JSONObject user = json.getJSONObject("user");

                        String accessToken = json.optString("access_token", null);
                        String refreshToken = json.optString("refresh_token", null);
                        String userId = user.optString("id", "");
                        String userEmail = user.optString("email", email);

                        sessionManager.saveSession(accessToken, refreshToken, userId, userEmail, "user", null);

                        profileService.fetchCurrentUserProfile(new SupabaseCallback<SupabaseProfileService.ProfileData>() {
                            @Override
                            public void onSuccess(SupabaseProfileService.ProfileData profileData) {
                                sessionManager.saveSession(accessToken, refreshToken, userId, userEmail, profileData.getRole(), profileData.getNome());
                                callback.onSuccess(new AuthUser(userId, userEmail, profileData.getNome()));
                            }

                            @Override
                            public void onError(String errorMessage) {
                                callback.onError(errorMessage);
                            }
                        });
                    } catch (Exception e) {
                        callback.onError("Nao foi possivel interpretar a resposta do login.");
                    }
                }
            });
        } catch (Exception e) {
            callback.onError("Nao foi possivel montar a requisicao de login.");
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
            if (message.isEmpty()) {
                return fallback;
            }
            return traduzirMensagem(message, fallback);
        } catch (Exception e) {
            return fallback;
        }
    }

    private String traduzirMensagem(String message, String fallback) {
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("user already registered")) {
            return "Ja existe uma conta cadastrada com este e-mail.";
        }
        if (lowerMessage.contains("password should be at least")) {
            return "A senha deve ter pelo menos 6 caracteres.";
        }
        if (lowerMessage.contains("invalid login credentials")) {
            return "E-mail ou senha invalidos.";
        }
        return message.isEmpty() ? fallback : message;
    }

    private void tratarErroLogin(String email, String responseBody, SupabaseCallback<AuthUser> callback) {
        String parsedError = parseSupabaseError(responseBody, "Nao foi possivel fazer login.");
        if (!"E-mail ou senha invalidos.".equals(parsedError)) {
            callback.onError(parsedError);
            return;
        }

        verificarSeEmailExiste(email, new SupabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean emailExiste) {
                if (Boolean.TRUE.equals(emailExiste)) {
                    callback.onError("Senha incorreta. Tente novamente.");
                } else {
                    callback.onError("Nenhuma conta encontrada para este e-mail.");
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(parsedError);
            }
        });
    }

    private void verificarSeEmailExiste(String email, SupabaseCallback<Boolean> callback) {
        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("email_input", email);

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "/rest/v1/rpc/email_exists_for_login")
                    .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .post(RequestBody.create(bodyJson.toString(), JSON))
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    callback.onError("Nao foi possivel validar o e-mail informado.");
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        callback.onError(responseBody.isEmpty() ? "Nao foi possivel validar o e-mail informado." : responseBody);
                        return;
                    }
                    callback.onSuccess(Boolean.parseBoolean(responseBody));
                }
            });
        } catch (Exception e) {
            callback.onError("Nao foi possivel montar a validacao do e-mail.");
        }
    }
}
