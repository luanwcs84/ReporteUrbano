package com.example.reporteurbano;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder> {

    private final Context context;
    private final List<Reporte> listaReportes;
    private final SupabaseReporteService reporteService;
    private final SupabaseStorageService storageService;

    public ReporteAdapter(Context context, List<Reporte> listaReportes) {
        this.context = context;
        this.listaReportes = new ArrayList<>(listaReportes);
        SessionManager sessionManager = new SessionManager(context);
        this.reporteService = new SupabaseReporteService(sessionManager);
        this.storageService = new SupabaseStorageService(sessionManager);
    }

    @NonNull
    @Override
    public ReporteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reporte, parent, false);
        return new ReporteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReporteViewHolder holder, int position) {
        Reporte reporteAtual = listaReportes.get(position);

        holder.txtTitulo.setText(reporteAtual.getTitulo());
        holder.txtLocal.setText(reporteAtual.getEndereco());
        holder.txtAutor.setText("Autor: " + reporteAtual.getAutorNome());

        Glide.with(context)
                .load(reporteAtual.getFotoUrl())
                .centerCrop()
                .into(holder.imgFoto);

        holder.btnDeletar.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Apagar reporte")
                    .setMessage("Tem certeza que deseja apagar este reporte?")
                    .setPositiveButton("Sim", (dialog, which) -> apagarReporte(reporteAtual, holder.getBindingAdapterPosition()))
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void apagarReporte(Reporte reporteAtual, int adapterPosition) {
        reporteService.deleteReporte(reporteAtual.getId(), new SupabaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (reporteAtual.getFotoUrl() != null && !reporteAtual.getFotoUrl().isEmpty()) {
                    storageService.deleteImageByPublicUrl(reporteAtual.getFotoUrl(), new SupabaseCallback<Void>() {
                        @Override
                        public void onSuccess(Void ignored) {
                        }

                        @Override
                        public void onError(String errorMessage) {
                        }
                    });
                }

                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (adapterPosition >= 0 && adapterPosition < listaReportes.size()) {
                        listaReportes.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                    }
                    Toast.makeText(context, "Reporte apagado com sucesso.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String errorMessage) {
                ((android.app.Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaReportes.size();
    }

    public void updateData(List<Reporte> novosReportes) {
        listaReportes.clear();
        listaReportes.addAll(novosReportes);
        notifyDataSetChanged();
    }

    static class ReporteViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtLocal, txtAutor;
        ImageView imgFoto, btnDeletar;

        public ReporteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtItemTitulo);
            txtLocal = itemView.findViewById(R.id.txtItemLocal);
            txtAutor = itemView.findViewById(R.id.txtItemAutor);
            imgFoto = itemView.findViewById(R.id.imgItemFoto);
            btnDeletar = itemView.findViewById(R.id.btnDeletarItem);
        }
    }
}
