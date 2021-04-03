package pendataan.parkir.kedungpane;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ListParkirAdapter extends FirestoreRecyclerAdapter<ParkirModel, ListParkirAdapter.ListParkirHolder> {
    private final Context context;
    private final RecyclerView recyclerView;

    public ListParkirAdapter(@NonNull FirestoreRecyclerOptions<ParkirModel> options, Context context) {
        super(options);
        this.context = context;
        recyclerView = ((Activity) context).findViewById(R.id.rvparkir);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull ListParkirHolder holder, int position, @NonNull ParkirModel model) {
        holder.nomormasuk.setText(String.valueOf(position+1));
        holder.platnomor.setText("Plat Nomor : "+model.getPlatNomor());
        holder.jeniskendaraan.setText("Kendaraan     : "+model.getJenisKendaraan());
        holder.keperluan.setText("Keperluan      : "+model.getKeperluan());
        holder.masukjam.setText("Masuk jam     : "+formatJam(model.getMasukJam()));
    }

    @NonNull
    @Override
    public ListParkirHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_kendaraanitem,
                parent, false);
        return new ListParkirHolder(v);
    }

    private void deleteData(int position){
        getSnapshots().getSnapshot(position).getReference().delete();
        stopListening();
        startListening();
        recyclerView.smoothScrollToPosition(0);
    }

    private void UpdateData(int position){
        getSnapshots().getSnapshot(position).getReference().update("keluarjam",new Date(),"sudahkeluar",true);
        stopListening();
        startListening();
        recyclerView.smoothScrollToPosition(0);
    }

    class ListParkirHolder extends RecyclerView.ViewHolder {
        TextView platnomor,jeniskendaraan,keperluan,masukjam,nomormasuk;
        ImageButton kelparkir, delparkir;

        public ListParkirHolder(View itemView) {
            super(itemView);
            platnomor = itemView.findViewById(R.id.platnomortext);
            jeniskendaraan = itemView.findViewById(R.id.jeniskendaraantext);
            keperluan = itemView.findViewById(R.id.keperluantext);
            masukjam = itemView.findViewById(R.id.masukjamtext);
            nomormasuk = itemView.findViewById(R.id.nomormasuk);
            kelparkir = itemView.findViewById(R.id.btn_kelparkir);
            delparkir = itemView.findViewById(R.id.btn_delparkir);
            delparkir.setOnClickListener(v -> {
                final int position = getAdapterPosition();
                final AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(context));
                builder.setTitle("Hapus Data Parkir");
                builder.setMessage("Yakin untuk menghapus data ini?");
                builder.setCancelable(false);
                builder.setPositiveButton("Iya", (dialog, which) -> deleteData(position));
                builder.setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss());
                builder.show();
            });
            kelparkir.setOnClickListener(v -> {
                final int position = getAdapterPosition();
                UpdateData(position);
            });
        }
    }

    private String formatJam(Date tanggal){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatTanggal = new SimpleDateFormat("HH:mm:ss");
        return formatTanggal.format(tanggal);
    }
}
