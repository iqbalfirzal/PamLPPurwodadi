package pendataan.parkir.kedungpane;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

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

public class OutParkir extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbRef =  db.collection("parkir");
    private BlurView blurbgform;
    private ListParkirAdapter adapter;
    private RecyclerView recyclerView;
    private Date ldate,gdate;
    private Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_out_parkir);
        blurbgform = findViewById(R.id.listparkir);
        EditText cari = findViewById(R.id.cariplatnomor);
        recyclerView = findViewById(R.id.rvparkir);
        ImageButton backlist = findViewById(R.id.btn_back_listparkir);
        setUpTanggal();
        query = dbRef.orderBy("masukjam")
                .whereGreaterThan("masukjam", new Timestamp(gdate))
                .whereLessThan("masukjam", new Timestamp(ldate)).whereEqualTo("sudahkeluar",false);
        setUpRecycleView();
        cari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.stopListening();
                if(s.toString().length()!=0){
                    query = dbRef.whereEqualTo("sudahkeluar",false).orderBy("masukjam")
                            .whereGreaterThan("masukjam", new Timestamp(gdate))
                            .whereLessThan("masukjam", new Timestamp(ldate))
                            .whereEqualTo("platnomor",s.toString());
                }else{
                    query = dbRef.orderBy("masukjam")
                            .whereGreaterThan("masukjam", new Timestamp(gdate))
                            .whereLessThan("masukjam", new Timestamp(ldate)).whereEqualTo("sudahkeluar",false);
                }
                setUpRecycleView();
                adapter.startListening();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        backlist.setOnClickListener(v -> {
            Intent intent = new Intent(OutParkir.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
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

    private  void setUpTanggal(){
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
        }
    }

    private  void setUpRecycleView(){
        FirestoreRecyclerOptions<ParkirModel> options = new FirestoreRecyclerOptions.Builder<ParkirModel>().setQuery(query, ParkirModel.class).build();
        adapter = new ListParkirAdapter(options, this);
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