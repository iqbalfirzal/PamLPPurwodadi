package pendataan.parkir.kedungpane;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;


public class Welcome extends AppCompatActivity {
    private FirebaseAuth auth;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        auth = FirebaseAuth.getInstance();
        ImageButton btnlogout = findViewById(R.id.btn_logout);
        TextView petugasnya = findViewById(R.id.petugasnya);
        petugasnya.setText(new PrefManager(this).getNama()+"\n"+new PrefManager(this).getRegu());
        Button etraffic = findViewById(R.id.btn_etraffic);
        Button econtrol = findViewById(R.id.btn_econtrol);
        Button elapsus = findViewById(R.id.btn_elapsus);
        Button gantipin = findViewById(R.id.btn_gantipin);
        Button eimei = findViewById(R.id.btn_eimei);
        btnlogout.setOnClickListener(v -> confirmRekapLogout());
        etraffic.setOnClickListener(v -> {
            Intent intent = new Intent(Welcome.this, MainActivity.class);
            startActivity(intent);
        });
        econtrol.setOnClickListener(v -> {
            Intent intent = new Intent(Welcome.this, MainActivity2.class);
            startActivity(intent);
        });
        elapsus.setOnClickListener(v -> {
            Intent intent = new Intent(Welcome.this, MainActivity3.class);
            startActivity(intent);
        });
        gantipin.setOnClickListener(v -> {
            Intent intent = new Intent(Welcome.this, ChangePin.class);
            startActivity(intent);
        });
        eimei.setOnClickListener(v -> showDialogDalamPengembangan());

    }

    private void showDialogDalamPengembangan(){
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Welcome.this);
        builder.setTitle("Mohon Maaf");
        builder.setMessage("Fitur ini masih dalam pengembangan.");
        builder.setCancelable(true);
        builder.setPositiveButton("Oke", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void confirmRekapLogout() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(Welcome.this);
        builder.setTitle("Keluar");
        builder.setMessage("Yakin ingin keluar?");
        builder.setCancelable(false);
        builder.setPositiveButton("Iya", (dialog, which) -> {
            ProgressDialog.show(Welcome.this, "Logout","Mohon tunggu...\nAnda akan otomatis logout setelah ini.", true,false);
            auth.signOut();
            clearLoginData();
            startActivity(new Intent(Welcome.this, Login.class));
            finish();
        });
        builder.setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void clearLoginData(){
        new PrefManager(Welcome.this.getApplicationContext()).clearData();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}