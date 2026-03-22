package com.example.reporteurbano;

public class Reporte {
    private int id;
    private String titulo;
    private String descricao;
    private String local;
    private String caminhoFoto;

    public Reporte(int id, String titulo, String descricao, String local, String caminhoFoto) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.local = local;
        this.caminhoFoto = caminhoFoto;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getLocal() { return local; }
    public String getCaminhoFoto() { return caminhoFoto; }
}