package pendataan.parkir.kedungpane;

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
    private EditText usernamenya, passwordnya;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        laymain = findViewById(R.id.layloginwelcome);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        usernamenya = findViewById(R.id.username);
        passwordnya = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        Button masuk = findViewById(R.id.btn_login);
        masuk.setOnClickListener(v -> {
            final String username = usernamenya.getText().toString() ;
            final String password = passwordnya.getText().toString() ;
            progressBar.setVisibility(View.VISIBLE);
            if(username.length() > 0 && password.length() > 0){
                DocumentReference loginguser = db.collection("petugas").document(username);
                loginguser.get().addOnSuccessListener(ds -> {
                    if (ds.get("password") != null) {
                        if (Objects.requireNonNull(ds.get("password")).toString().equals(password)) {
                            String userRegu = Objects.requireNonNull(ds.get("regu")).toString();
                            performLogin(username, password, userRegu);
                        }else{
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, "Password salah!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "Username tidak terdaftar!", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Login gagal. Mohon periksa koneksi.", Toast.LENGTH_LONG).show();
                });

            }else{
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Login.this, "Username atau Password kosong!", Toast.LENGTH_LONG).show();
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

    private void performLogin(String username, String password, String regu) {
            auth.signInWithEmailAndPassword("wasrik1@lpsmg.go.id","wasrik1").addOnCompleteListener(Login.this, task -> {
                progressBar.setVisibility(View.GONE);
                if(!task.isSuccessful()){
                    Toast.makeText(Login.this, "Password salah.",
                            Toast.LENGTH_LONG).show();
                }else{
                    saveLoginDetails(username, password, regu);
                    Intent intent = new Intent(Login.this, Welcome.class);
                    startActivity(intent);
                    finish();
                }
            });
    }

    private void saveLoginDetails(String username, String password, String regu){
        new PrefManager(this).saveLoginDetails(username, password, regu);
    }

}