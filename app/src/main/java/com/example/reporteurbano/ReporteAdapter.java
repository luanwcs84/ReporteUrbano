package com.example.reporteurbano;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder> {

    private List<Reporte> listaReportes;

    // Construtor: O Adapter recebe a lista de dados quando é criado
    public ReporteAdapter(List<Reporte> listaReportes) {
        this.listaReportes = listaReportes;
    }

    @NonNull
    @Override
    public ReporteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reporte, parent, false);
        return new ReporteViewHolder(view);
    }

    // Preenche os dados no cartão
    @Override
    public void onBindViewHolder(@NonNull ReporteViewHolder holder, int position) {
        Reporte reporteAtual = listaReportes.get(position);
        holder.txtTitulo.setText(reporteAtual.getTitulo());
        holder.txtLocal.setText(reporteAtual.getLocal());

        // Preenche a foto guardada no armazenamento oculto
        File arquivoFoto = new File(reporteAtual.getCaminhoFoto());
        if (arquivoFoto.exists()) {
            Bitmap bitmapFoto = BitmapFactory.decodeFile(arquivoFoto.getAbsolutePath());
            holder.imgFoto.setImageBitmap(bitmapFoto);
        }
    }

    @Override
    public int getItemCount() {
        return listaReportes.size();
    }

    static class ReporteViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtLocal;
        ImageView imgFoto;

        public ReporteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtItemTitulo);
            txtLocal = itemView.findViewById(R.id.txtItemLocal);
            imgFoto = itemView.findViewById(R.id.imgItemFoto);
        }
    }
}