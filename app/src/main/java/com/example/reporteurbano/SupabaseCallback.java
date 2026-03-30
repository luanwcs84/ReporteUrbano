package com.example.reporteurbano;

public interface SupabaseCallback<T> {
    void onSuccess(T result);
    void onError(String errorMessage);
}
