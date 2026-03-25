package com.example.reporteurbano;

public class Reporte {
    private final String id;
    private final String userId;
    private final String titulo;
    private final String descricao;
    private final String endereco;
    private final double latitude;
    private final double longitude;
    private final String fotoUrl;
    private final String autorNome;
    private final String autorEmail;
    private final String createdAt;

    public Reporte(String id,
                   String userId,
                   String titulo,
                   String descricao,
                   String endereco,
                   double latitude,
                   double longitude,
                   String fotoUrl,
                   String autorNome,
                   String autorEmail,
                   String createdAt) {
        this.id = id;
        this.userId = userId;
        this.titulo = titulo;
        this.descricao = descricao;
        this.endereco = endereco;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fotoUrl = fotoUrl;
        this.autorNome = autorNome;
        this.autorEmail = autorEmail;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getEndereco() { return endereco; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getFotoUrl() { return fotoUrl; }
    public String getAutorNome() { return autorNome; }
    public String getAutorEmail() { return autorEmail; }
    public String getCreatedAt() { return createdAt; }
}
