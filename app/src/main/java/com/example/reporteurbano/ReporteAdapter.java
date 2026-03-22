package com.example.reporteurbano;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class ReporteAdapter extends RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder> {

    private Context context;
    private List<Reporte> listaReportes;

    public ReporteAdapter(Context context, List<Reporte> listaReportes) {
        this.context = context;
        this.listaReportes = listaReportes;
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
        holder.txtLocal.setText(reporteAtual.getLocal());

        File arquivoFoto = new File(reporteAtual.getCaminhoFoto());
        if (arquivoFoto.exists()) {
            Bitmap bitmapFoto = BitmapFactory.decodeFile(arquivoFoto.getAbsolutePath());
            holder.imgFoto.setImageBitmap(bitmapFoto);
        }

        holder.btnDeletar.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Apagar Reporte")
                    .setMessage("Tem certeza que deseja apagar este reporte? Esta ação não pode ser desfeita.")

                    .setPositiveButton("Sim", (dialog, which) -> {
                        DatabaseHelper dbHelper = new DatabaseHelper(context);

                        dbHelper.deletarReporte(reporteAtual.getId());
                        if (arquivoFoto.exists()) {
                            arquivoFoto.delete();
                        }

                        listaReportes.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, listaReportes.size());

                        Toast.makeText(context, "Reporte apagado!", Toast.LENGTH_SHORT).show();
                    })

                    .setNegativeButton("Cancelar", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return listaReportes.size();
    }

    static class ReporteViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtLocal;
        ImageView imgFoto, btnDeletar;

        public ReporteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtItemTitulo);
            txtLocal = itemView.findViewById(R.id.txtItemLocal);
            imgFoto = itemView.findViewById(R.id.imgItemFoto);
            btnDeletar = itemView.findViewById(R.id.btnDeletarItem);
        }
    }
}