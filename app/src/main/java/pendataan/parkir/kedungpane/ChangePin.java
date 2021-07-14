package pendataan.parkir.kedungpane;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class ChangePin extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private BlurView blurbgform;
    private EditText namapengguna, pinpengguna;
    private Spinner reguopt;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);
        blurbgform =  findViewById(R.id.formeditpin);
        namapengguna = findViewById(R.id.namapengguna);
        pinpengguna = findViewById(R.id.pinpengguna);
        reguopt = findViewById(R.id.regu_opt);
        Button gantipin = findViewById(R.id.btn_gantipin);
        ImageButton back = findViewById(R.id.btn_back_changepin);
        gantipin.setOnClickListener(v -> {
            if(TextUtils.isEmpty(namapengguna.getText().toString())
                    |TextUtils.isEmpty(pinpengguna.getText().toString())|reguopt.getSelectedItem()==null){
                Toast.makeText(getApplication(), "Masih ada kolom yang kosong.", Toast.LENGTH_SHORT).show();
            }else{
                confirmGantiPin();
            }
        });
        back.setOnClickListener(v -> finish());
        blurinForm();
        setDataSemula();
        setUpSpinnerRegu();
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

    private void setDataSemula(){
        namapengguna.setText(new PrefManager(this).getNama());
        pinpengguna.setText(new PrefManager(this).getPin());
    }

    private void setUpSpinnerRegu() {
        List<String> list = new ArrayList<>();
        list.add("REGU 1");list.add("REGU 2");list.add("REGU 3");list.add("REGU 4");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reguopt.setAdapter(dataAdapter);
        String selectedList = new PrefManager(this).getRegu();
        for(int i=0; i < dataAdapter.getCount(); i++) {
            if (selectedList.trim().equals(dataAdapter.getItem(i))) {
                reguopt.setSelection(i);
                break;
            }
        }
    }

    private void confirmGantiPin(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Perbarui Data Akun");
        builder.setMessage("Pastikan seluruh data sudah benar?");
        builder.setCancelable(false);
        builder.setPositiveButton("Iya", (dialog, which) -> {
            progressDialog = new ProgressDialog(ChangePin.this);
            progressDialog.isIndeterminate();
            progressDialog.setMessage("Sedang mengunggah data...");
            progressDialog.setTitle("Perbarui Data Akun");
            progressDialog.setCancelable(false);
            progressDialog.show();
            kirimData();
        });
        builder.setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void kirimData(){
        Map<String, Object> docData = new HashMap<>();
        docData.put("nama", String.valueOf(namapengguna.getText()));
        docData.put("pin", String.valueOf(pinpengguna.getText()));
        docData.put("regu", reguopt.getSelectedItem().toString());
        db.collection("petugas").document(new PrefManager(this).getNip())
                .set(docData)
                .addOnSuccessListener(aVoid -> {
                    new PrefManager(this).updateLoginDetails(String.valueOf(namapengguna.getText()), String.valueOf(pinpengguna.getText()), reguopt.getSelectedItem().toString());progressDialog.dismiss();
                    Toast.makeText(ChangePin.this,"Data berhasil diperbarui.",Toast.LENGTH_LONG).show();finish();})
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(ChangePin.this,"Gagal menambahkan data! Periksa koneksi.",Toast.LENGTH_LONG).show();
                });
    }

}