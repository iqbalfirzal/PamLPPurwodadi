package pendataan.parkir.kedungpane;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton btnmasuk = findViewById(R.id.btn_posmasuk);
        ImageButton btnkeluar = findViewById(R.id.btn_poskeluar);
        btnmasuk.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddParkir.class);
            startActivity(intent);
            finish();
        });
        btnkeluar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OutParkir.class);
            startActivity(intent);
            finish();
        });
    }
}