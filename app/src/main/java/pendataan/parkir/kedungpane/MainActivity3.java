package pendataan.parkir.kedungpane;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity3 extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText namapelapor, isilaporan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        namapelapor = findViewById(R.id.namapelapor);
        isilaporan = findViewById(R.id.isilaporan);
        Button kirim = findViewById(R.id.btn_kirimlaporan);
        ImageButton back = findViewById(R.id.btn_back_addlaporan);
        kirim.setOnClickListener(v -> {
            if(TextUtils.isEmpty(namapelapor.getText().toString())|TextUtils.isEmpty(isilaporan.getText().toString())){
                Toast.makeText(getApplication(), "Mohon Isi Seluruh Kolom.", Toast.LENGTH_SHORT).show();
            }else{
                confirmKirim();
            }
        });
        back.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity3.this, Welcome.class);
            startActivity(intent);
            finish();
        });
    }

    private void confirmKirim(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kirim Laporan ?");
        builder.setMessage("Ingat untuk tidak mengirim laporan yang salah.");
        builder.setCancelable(false);
        builder.setPositiveButton("Kirim", (dialog, which) -> kirimData());
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void kirimData(){
        Map<String, Object> docData = new HashMap<>();
        docData.put("isilaporan", String.valueOf(isilaporan.getText()));
        docData.put("namapelapor", String.valueOf(namapelapor.getText()));
        docData.put("tgllaporan", new Date());
        db.collection("lapsus")
                .document().set(docData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity3.this,"Data berhasil disimpan.",Toast.LENGTH_LONG).show();finish();
                }).addOnFailureListener(e -> Toast.makeText(MainActivity3.this,"Gagal menambahkan data! Periksa koneksi.",Toast.LENGTH_LONG).show());
    }

}