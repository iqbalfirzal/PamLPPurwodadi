package pendataan.parkir.kedungpane;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class HistoryParkir extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbRef =  db.collection("parkir");
    private BlurView blurbgform;
    private ListHistoriParkirAdapter adapter;
    private RecyclerView recyclerView;
    private Query query;
    private Date ldate,gdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_parkir);
        blurbgform = findViewById(R.id.listparkir);
        EditText cari = findViewById(R.id.cariplatnomor);
        recyclerView = findViewById(R.id.rvparkir);
        ImageButton backlist = findViewById(R.id.btn_back_listhistoriparkir);
        setUpRecycleView();
        cari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.stopListening();
                if(s.toString().length()!=0){
                    query = dbRef.orderBy("platnomor")
                            .whereEqualTo("sudahkeluar",true).startAt(s.toString().toUpperCase().trim()).endAt(s.toString().toUpperCase().trim() + "\uf8ff");
                }else{
                    query = dbRef.orderBy("masukjam")
                            .whereGreaterThan("masukjam", new Timestamp(gdate))
                            .whereLessThan("masukjam", new Timestamp(ldate)).whereEqualTo("sudahkeluar",true);
                }
                setUpRecycleView();
                adapter.startListening();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        cari.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId== EditorInfo.IME_ACTION_SEARCH){
                adapter.notifyDataSetChanged();
            }
            return false;
        });
        backlist.setOnClickListener(v -> finish());
        blurinForm();
    }

    private void blurinForm(){
        float radius = 20f;
        View decorView = getWindow().getDecorView();
        ViewGroup rootView = decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();
        blurbgform.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setHasFixedTransformationMatrix(true);
    }

    private  void setUpRecycleView(){
        Date today = new Date();
        SimpleDateFormat todayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateFormated = todayDateFormat.format(today);
        String lessDate = dateFormated+" 23:59:59";
        String greatDate = dateFormated+" 00:00:01";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault());
        try{
            ldate = dateFormat.parse(lessDate);
            gdate = dateFormat.parse(greatDate);
        } catch (ParseException e){
            e.printStackTrace();
        }assert ldate != null;assert gdate != null;
        query = dbRef.orderBy("masukjam")
                .whereGreaterThan("masukjam", new Timestamp(gdate))
                .whereLessThan("masukjam", new Timestamp(ldate)).whereEqualTo("sudahkeluar",true);

        FirestoreRecyclerOptions<ParkirModel> options = new FirestoreRecyclerOptions.Builder<ParkirModel>().setQuery(query, ParkirModel.class).build();
        adapter = new ListHistoriParkirAdapter(options, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}