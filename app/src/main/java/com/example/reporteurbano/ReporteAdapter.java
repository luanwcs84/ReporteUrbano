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

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder> {
    private static final DateTimeFormatter INPUT_OFFSET_DATE_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter INPUT_LOCAL_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter INPUT_DATE_ONLY_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter OUTPUT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));

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
        holder.txtEmailAutor.setText("E-mail: " + reporteAtual.getAutorEmail());
        holder.txtData.setText("Data: " + formatarData(reporteAtual.getCreatedAt()));

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

    private String formatarData(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) {
            return "Nao informada";
        }

        try {
            return OffsetDateTime.parse(createdAt, INPUT_OFFSET_DATE_FORMAT).format(OUTPUT_DATE_FORMAT);
        } catch (Exception ignored) {
            try {
                return LocalDateTime.parse(createdAt, INPUT_LOCAL_DATE_FORMAT).format(OUTPUT_DATE_FORMAT);
            } catch (Exception ignoredAgain) {
                try {
                    return LocalDate.parse(createdAt, INPUT_DATE_ONLY_FORMAT).format(OUTPUT_DATE_FORMAT);
                } catch (Exception ignoredOneMore) {
                    return createdAt.length() >= 10 ? createdAt.substring(0, 10) : createdAt;
                }
            }
        }
    }

    static class ReporteViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo;
        TextView txtLocal;
        TextView txtAutor;
        TextView txtEmailAutor;
        TextView txtData;
        ImageView imgFoto;
        ImageView btnDeletar;

        public ReporteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtItemTitulo);
            txtLocal = itemView.findViewById(R.id.txtItemLocal);
            txtAutor = itemView.findViewById(R.id.txtItemAutor);
            txtEmailAutor = itemView.findViewById(R.id.txtItemEmailAutor);
            txtData = itemView.findViewById(R.id.txtItemData);
            imgFoto = itemView.findViewById(R.id.imgItemFoto);
            btnDeletar = itemView.findViewById(R.id.btnDeletarItem);
        }
    }
}
