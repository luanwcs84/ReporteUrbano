package com.example.reporteurbano;

public class Reporte {
    private final String id;
    private final String titulo;
    private final String descricao;
    private final String endereco;
    private final double latitude;
    private final double longitude;
    private final String fotoUrl;

    public Reporte(String id, String titulo, String descricao, String endereco, double latitude, double longitude, String fotoUrl) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.endereco = endereco;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fotoUrl = fotoUrl;
    }

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getEndereco() { return endereco; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getFotoUrl() { return fotoUrl; }
}
