package com.example.reporteurbano;

public final class SupabaseConfig {

    private SupabaseConfig() {
    }

    public static final String SUPABASE_URL = "https://mawxszkjzjgtrpifxdid.supabase.co";
    public static final String SUPABASE_ANON_KEY = "sb_publishable_BbSrEDsCVft4kg3sXEHFEg_aNcK9f6X";
    public static final String STORAGE_BUCKET = "reportes-fotos";

    public static boolean isConfigured() {
        return !SUPABASE_URL.contains("SEU-PROJETO")
                && !SUPABASE_ANON_KEY.contains("SUA_ANON_KEY");
    }
}
