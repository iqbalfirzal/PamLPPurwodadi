package pendataan.parkir.kedungpane;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.Date;
import java.util.Objects;

public class ListParkirAdapter extends FirestoreRecyclerAdapter<ParkirModel, ListParkirAdapter.ListParkirHolder> {
    private final Context context;
    private ProgressDialog progressDialog;

    public ListParkirAdapter(@NonNull FirestoreRecyclerOptions<ParkirModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull ListParkirHolder holder, int position, @NonNull ParkirModel model) {
        holder.platnomor.setText("Plat Nomor : "+model.getPlatNomor());
        holder.jeniskendaraan.setText("Jenis Kendaraan : "+model.getJenisKendaraan());
    }

    @NonNull
    @Override
    public ListParkirHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_kendaraanitem,
                parent, false);
        return new ListParkirHolder(v);
    }

    class ListParkirHolder extends RecyclerView.ViewHolder {
        TextView platnomor, jeniskendaraan;
        ImageButton kelparkir, delparkir;

        public ListParkirHolder(View itemView) {
            super(itemView);
            platnomor = itemView.findViewById(R.id.platnomortext);
            jeniskendaraan = itemView.findViewById(R.id.jeniskendaraantext);
            kelparkir = itemView.findViewById(R.id.btn_kelparkir);
            delparkir = itemView.findViewById(R.id.btn_delparkir);
            delparkir.setOnClickListener(v -> {
                final int position = getAdapterPosition();
                final AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(context));
                builder.setTitle("Hapus Data Parkir");
                builder.setMessage("Yakin untuk menghapus data ini?");
                builder.setCancelable(false);
                builder.setPositiveButton("Iya", (dialog, which) -> {
                    progressDialog = new ProgressDialog(context);
                    progressDialog.isIndeterminate();
                    progressDialog.setMessage("Sedang menghapus data...");
                    progressDialog.setTitle("Hapus Data Parkir");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    deleteData(position);
                });
                builder.setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss());
                builder.show();
            });
            kelparkir.setOnClickListener(v -> {
                final int position = getAdapterPosition();
                UpdateData(position);
            });
        }
    }

    private void deleteData(int position){
        getSnapshots().getSnapshot(position).getReference().delete();
        notifyItemRemoved(position);
        progressDialog.dismiss();
    }

    private void UpdateData(int position){
        getSnapshots().getSnapshot(position).getReference().update("keluarjam",new Date(),"sudahkeluar",true);
        notifyItemRemoved(position);
        progressDialog.dismiss();
    }
}
