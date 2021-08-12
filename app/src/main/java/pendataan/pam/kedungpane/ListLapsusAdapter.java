package pendataan.pam.kedungpane;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
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

public class ListLapsusAdapter extends FirestoreRecyclerAdapter<LapsusModel, ListLapsusAdapter.ListLapsusHolder> {
    private final Context context;
    private final RecyclerView recyclerView;

    public ListLapsusAdapter(@NonNull FirestoreRecyclerOptions<LapsusModel> options, Context context) {
        super(options);
        this.context = context;
        recyclerView = ((Activity) context).findViewById(R.id.rvlaporan);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull ListLapsusHolder holder, int position, @NonNull LapsusModel model) {
        holder.jamlaporan.setText("Pukul\n"+formatJam(model.getTglLaporan()));
        holder.namapelapor.setText("Pelapor : "+model.getNamaPelapor());
        holder.tanggallaporan.setText("Tanggal   : "+formatTanggal(model.getTglLaporan()));
        holder.isilaporan.setText(model.getIsiLaporan());
        Log.i("TESI", model.getInstruksiPim());
        if(model.getInstruksiPim().equals("")){
            holder.instruksi.setText("-");
        }else{
            holder.instruksi.setText(model.getInstruksiPim());
        }
    }

    @NonNull
    @Override
    public ListLapsusHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_lapsusitem,
                parent, false);
        return new ListLapsusHolder(v);
    }

    class ListLapsusHolder extends RecyclerView.ViewHolder {
        TextView jamlaporan,namapelapor,tanggallaporan,isilaporan,instruksi;

        public ListLapsusHolder(View itemView) {
            super(itemView);
            jamlaporan = itemView.findViewById(R.id.jamlaporan);
            namapelapor = itemView.findViewById(R.id.namapelapor);
            tanggallaporan = itemView.findViewById(R.id.tanggallaporan);
            isilaporan = itemView.findViewById(R.id.isilaporan);
            instruksi = itemView.findViewById(R.id.instruksi);
            recyclerView.scrollToPosition(0);
        }
    }

    private String formatJam(Date tanggal){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatTanggal = new SimpleDateFormat("HH:mm");
        return formatTanggal.format(tanggal);
    }
    private String formatTanggal(Date tanggal){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatTanggal = new SimpleDateFormat("EEEE, dd MMMM yyy");
        return formatTanggal.format(tanggal);
    }
}
