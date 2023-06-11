package pendataan.pam.ambarawa;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikhaellopez.circularimageview.CircularImageView;

public class ShowEmergencyCall extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_emergency_call);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        TextView nip = findViewById(R.id.shownipnya);
        TextView nama = findViewById(R.id.shownamanya);
        TextView regu = findViewById(R.id.showregunya);
        CircularImageView foto = findViewById(R.id.showfotonya);
        Button openmap = findViewById(R.id.btn_openmap);
        if (getIntent().hasExtra("senderid")){
            String senderid = getIntent().getStringExtra("senderid");
            String sendername = getIntent().getStringExtra("sendername");
            String senderregu = getIntent().getStringExtra("senderregu");
            String senderphoto = getIntent().getStringExtra("senderphoto");
            String senderlocationlat = getIntent().getStringExtra("senderlocation_lat");
            String senderlocationlongi = getIntent().getStringExtra("senderlocation_longi");
            Glide.with(this)
                    .load(senderphoto)
                    .placeholder(R.drawable.ic_account)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop()
                    .into(foto);
            nip.setText(senderid);nama.setText(sendername);regu.setText(senderregu);
            openmap.setOnClickListener(v -> {
                String strUri = "http://maps.google.com/maps?q=loc:" + senderlocationlat + "," + senderlocationlongi + "(pinned)";
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(strUri));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            });
        }else {
            finish();
        }
    }
}