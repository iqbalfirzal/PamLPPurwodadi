package pendataan.pengamanan.rtpwd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class Login extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ConstraintLayout laymain;
    private EditText nipnya, pinnya;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        laymain = findViewById(R.id.layloginwelcome);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        nipnya = findViewById(R.id.nip);
        pinnya = findViewById(R.id.pin);
        progressBar = findViewById(R.id.progressBar);
        Button masuk = findViewById(R.id.btn_login);
        masuk.setOnClickListener(v -> {
            final String nip = nipnya.getText().toString() ;
            final String pin = pinnya.getText().toString() ;
            progressBar.setVisibility(View.VISIBLE);
            if(nip.length() > 0 && pin.length() > 0){
                DocumentReference loginguser = db.collection("petugas").document(nip);
                loginguser.get().addOnSuccessListener(ds -> {
                    if (ds.get("pin") != null) {
                        if (Objects.requireNonNull(ds.get("pin")).toString().equals(pin)) {
                            String userNama =  Objects.requireNonNull(ds.get("nama")).toString();
                            String userRegu = Objects.requireNonNull(ds.get("regu")).toString();
                            String userFoto = Objects.requireNonNull(ds.get("foto")).toString();
                            performLogin(nip, userNama, pin, userRegu, userFoto);
                        }else{
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, "Password salah!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "NIP tidak terdaftar!", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Login gagal. Mohon periksa koneksi.", Toast.LENGTH_LONG).show();
                });

            }else{
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Login.this, "NIP atau PIN kosong!", Toast.LENGTH_LONG).show();
            }
        });

        networkChecking();
    }

    private void networkChecking(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            checkLoggedIn();
        } else {
            showNoConnectionNoticeDialog();
        }
    }

    @SuppressLint("SetTextI18n")
    private void showNoConnectionNoticeDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tidak dapat terhubung");
        builder.setMessage("• Aplikasi tidak akan bekerja dengan baik tanpa koneksi internet.\n\n• Pastikan koneksi wifi atau data memiliki akses internet.");
        builder.setCancelable(false);
        builder.setPositiveButton("Oke", (dialog, which) -> {
            dialog.dismiss();
            finishAndRemoveTask();
        });
        builder.show();
    }

    @SuppressLint("SetTextI18n")
    private void checkLoggedIn(){
        if (!new PrefManager(this).isUserLogedOut()) {
            Intent intent = new Intent(Login.this, Welcome.class);
            startActivity(intent);
            finish();
        }else{
            ConstraintSet set = new ConstraintSet();
            set.setVisibilityMode(R.id.layloginwelcome, ConstraintSet.GONE);
            set.applyTo(laymain);
        }
    }

    private void performLogin(final String nip, String nama, String pin, String regu, String foto) {
        auth.signInWithEmailAndPassword("pengamanan1.1@rtpwd.go.id","pengamanan1.1").addOnCompleteListener(Login.this, task -> {
            progressBar.setVisibility(View.GONE);
            if(!task.isSuccessful()){
                Toast.makeText(Login.this,"Gagal login! Periksa koneksi.",Toast.LENGTH_LONG).show();
            }else{
                saveLoginDetails(nip, nama, pin, regu, foto);
                Intent intent = new Intent(Login.this, Welcome.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void saveLoginDetails(String nip, String nama, String password, String regu, String foto){
        new PrefManager(this).saveLoginDetails(nip, nama, password, regu, foto);
    }

}