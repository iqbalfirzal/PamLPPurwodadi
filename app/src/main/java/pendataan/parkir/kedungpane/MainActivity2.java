package pendataan.parkir.kedungpane;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity implements View.OnClickListener {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText namapetugas;
    private Spinner regu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        namapetugas = findViewById(R.id.namapetugas);
        regu = findViewById(R.id.regu_opt);
        Button scan = findViewById(R.id.btn_scan);
        scan.setOnClickListener(this);
        setUpSpinnerRegu();
    }

    private void setUpSpinnerRegu() {
        List<String> list = new ArrayList<>();
        list.add("REGU 1");list.add("REGU 2");list.add("REGU 3");list.add("REGU 4");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regu.setAdapter(dataAdapter);
    }

    @Override
    public void onClick(View v) {
        if(TextUtils.isEmpty(namapetugas.getText().toString())|regu.getSelectedItem()==null){
            Toast.makeText(getApplication(), "Mohon Isi Nama Petugas.", Toast.LENGTH_SHORT).show();
        }else{
            IntentIntegrator intentIntegrator = new IntentIntegrator(this);
            intentIntegrator.setPrompt("Scan Pada QR Kontrol");
            intentIntegrator.setOrientationLocked(true);
            intentIntegrator.initiateScan();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Gagal Membaca, coba lagi.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject obj = new JSONObject(intentResult.getContents());
                    String namalp = (obj.getString("namalp"));
                    String namacekpoin = (obj.getString("namacekpoin"));
                    coonfirmKirimData(namalp,namacekpoin);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, intentResult.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void coonfirmKirimData(String namalp, String namacekpoin){
        if(namalp.equals("kedungpane")){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Kirim Laporan Kontrol ?");
            builder.setMessage("Nama Petugas : "+namapetugas.getText().toString()
                    +"\nRegu : "+regu.getSelectedItem().toString()+"\nPos Kontrol : "+namacekpoin);
            builder.setCancelable(false);
            builder.setPositiveButton("Kirim", (dialog, which) -> kirimData(namacekpoin));
            builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
            builder.show();
        }else{
            Toast.makeText(MainActivity2.this,"Scan QR Code yang telah ditentukan.",Toast.LENGTH_LONG).show();
        }
    }

    private void kirimData(String namacekpoin){
        Map<String, Object> docData = new HashMap<>();
        docData.put("jamkontrol", new Date());
        docData.put("petugas", String.valueOf(namapetugas.getText()));
        docData.put("poskontrol", namacekpoin);
        docData.put("regu", regu.getSelectedItem().toString());
        db.collection("kontrolwasrik")
                .document().set(docData)
                .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity2.this,"Laporan berhasil terkirim.",Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(MainActivity2.this,"Gagal menambahkan data! Periksa koneksi.",Toast.LENGTH_LONG).show());
    }

}