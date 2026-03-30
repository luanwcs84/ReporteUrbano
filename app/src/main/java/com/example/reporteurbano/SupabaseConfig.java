package com.example.reporteurbano;

public final class SupabaseConfig {

    private static final String PLACEHOLDER_URL = "https://seu-projeto.supabase.co";
    private static final String PLACEHOLDER_ANON_KEY = "sua_publishable_key_aqui";

    private SupabaseConfig() {
    }

    public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;
    public static final String STORAGE_BUCKET = BuildConfig.SUPABASE_STORAGE_BUCKET;

    public static boolean isConfigured() {
        return !SUPABASE_URL.equals(PLACEHOLDER_URL)
                && !SUPABASE_ANON_KEY.equals(PLACEHOLDER_ANON_KEY);
    }
}
