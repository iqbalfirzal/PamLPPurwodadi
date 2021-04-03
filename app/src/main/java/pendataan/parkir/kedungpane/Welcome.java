package pendataan.parkir.kedungpane;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;

import java.util.Objects;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Button etraffic = findViewById(R.id.btn_etraffic);
        Button econtrol = findViewById(R.id.btn_econtrol);
        Button elapsus = findViewById(R.id.btn_elapsus);
        Button eimei = findViewById(R.id.btn_eimei);
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
        eimei.setOnClickListener(v -> {
            showDialogDalamPengembangan();
        });

    }

    private void showDialogDalamPengembangan(){
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(this));
        LayoutInflater inflater = getLayoutInflater();
        builder.setTitle("Mohon Maaf");
        builder.setMessage("Fitur ini masih dalam pengembangan.");
        builder.setCancelable(true);
        builder.setPositiveButton("Oke", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}