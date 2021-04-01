package pendataan.parkir.kedungpane;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class AddParkir extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private BlurView blurbgform;
    private EditText plat;
    private Spinner jk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parkir);
        blurbgform = findViewById(R.id.formtambahkendaraan);
        plat = findViewById(R.id.platnomor);
        jk = findViewById(R.id.jeniskendaraan_opt);
        Button addparkir = findViewById(R.id.btn_daftarparkir);
        ImageButton backaddparkir = findViewById(R.id.btn_back_addparkir);
        setUpSpinnerJK();
        addparkir.setOnClickListener(v -> {
            if(TextUtils.isEmpty(plat.getText().toString())|jk.getSelectedItem()==null){
                Toast.makeText(getApplication(), "Mohon Isi Plat Nomor.", Toast.LENGTH_SHORT).show();
            }else{
                addDataParkir();
            }
        });
        backaddparkir.setOnClickListener(v -> {
            Intent intent = new Intent(AddParkir.this, MainActivity.class);
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

    private void setUpSpinnerJK() {
        List<String> list = new ArrayList<>();
        list.add("Motor");list.add("Mobil");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jk.setAdapter(dataAdapter);
    }

    private void addDataParkir(){
        Map<String, Object> docData = new HashMap<>();
        docData.put("jeniskendaraan", jk.getSelectedItem().toString());
        docData.put("keluarjam", new Date());
        docData.put("masukjam", new Date());
        docData.put("platnomor", String.valueOf(plat.getText()));
        docData.put("sudahkeluar", false);
        db.collection("parkir").document()
                .set(docData)
                .addOnSuccessListener(aVoid -> {
                    plat.setText("");
                    Toast.makeText(AddParkir.this,"Data berhasil disimpan",Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> Toast.makeText(AddParkir.this,"Gagal menambahkan data! Periksa koneksi.",Toast.LENGTH_LONG).show());
    }

}