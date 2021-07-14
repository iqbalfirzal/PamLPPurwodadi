package pendataan.parkir.kedungpane;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ListHistoriParkirAdapter extends FirestoreRecyclerAdapter<ParkirModel, ListHistoriParkirAdapter.ListHistoriParkirHolder> {
    private final Context context;

    public ListHistoriParkirAdapter(@NonNull FirestoreRecyclerOptions<ParkirModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull ListHistoriParkirHolder holder, int position, @NonNull ParkirModel model) {
        holder.nomormasuk.setText(String.valueOf(position+1));
        holder.platnomor.setText("Plat Nomor : "+model.getPlatNomor());
        holder.jeniskendaraan.setText("Kendaraan     : "+model.getJenisKendaraan());
        holder.keperluan.setText("Keperluan      : "+model.getKeperluan());
        holder.masukjam.setText("Masuk jam     : "+formatJam(model.getMasukJam()));
        holder.keluarjam.setText("Keluar jam     : "+formatJam(model.getKeluarJam()));
    }

    @NonNull
    @Override
    public ListHistoriParkirHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_historikendaraanitem,
                parent, false);
        return new ListHistoriParkirHolder(v);
    }

    static class ListHistoriParkirHolder extends RecyclerView.ViewHolder {
        TextView platnomor,jeniskendaraan,keperluan,masukjam,keluarjam,nomormasuk;

        public ListHistoriParkirHolder(View itemView) {
            super(itemView);
            platnomor = itemView.findViewById(R.id.platnomortext);
            jeniskendaraan = itemView.findViewById(R.id.jeniskendaraantext);
            keperluan = itemView.findViewById(R.id.keperluantext);
            masukjam = itemView.findViewById(R.id.masukjamtext);
            keluarjam = itemView.findViewById(R.id.keluarjamtext);
            nomormasuk = itemView.findViewById(R.id.nomormasuk);
        }
    }

    private String formatJam(Date tanggal){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatTanggal = new SimpleDateFormat("HH:mm:ss");
        return formatTanggal.format(tanggal);
    }
}
