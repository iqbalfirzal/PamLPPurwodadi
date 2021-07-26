package pendataan.parkir.kedungpane;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Welcome extends AppCompatActivity {
    private FirebaseAuth auth;
    private RequestQueue mRequestQue;
    private final String SENDNOTIFURL = "https://fcm.googleapis.com/fcm/send";
    private static final int REQUEST_FINE_LOCATION_CODE = 1111;
    private static final int REQUEST_COARSE_LOCATION_CODE = 1101;
    LocationManager locationManager;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        FirebaseMessaging.getInstance().subscribeToTopic("umjoL1srorNjDvpmeocBJ1kN7pVTb4t9zgmsPCHIs");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        TextView intruksipim = findViewById(R.id.instruksipim);
        intruksipim.setText(new PrefManager(this).getInstruksiPim());
        DocumentReference isiinstruksi = db.collection("instruksipimpinan").document("untukpam");
        isiinstruksi.get().addOnSuccessListener(ds -> {
            if (!Objects.equals(ds.get("kalapas"), "")) {
                String ikalapas =  Objects.requireNonNull(ds.get("kalapas")).toString();
                String ikplp = Objects.requireNonNull(ds.get("kplp")).toString();
                intruksipim.setText("[ KALAPAS ] "+ikalapas+"\n\n[ KPLP ] "+ikplp);
                new PrefManager(this).saveInstruksiPim("[ KALAPAS ] "+ikalapas+"\n\n[ KPLP ] "+ikplp);
            } else {
                intruksipim.setText(new PrefManager(this).getInstruksiPim());
            }
        }).addOnFailureListener(e -> Toast.makeText(Welcome.this, "Gagal memuat instruksi pimpinan. Periksa koneksi Anda.", Toast.LENGTH_LONG).show());
        mRequestQue = Volley.newRequestQueue(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ImageButton btnlogout = findViewById(R.id.btn_logout);
        TextView petugasnya = findViewById(R.id.petugasnya);
        petugasnya.setText(new PrefManager(this).getNama()+"\n"+new PrefManager(this).getRegu());
        Button emergencycall = findViewById(R.id.btn_emergencycall);
        Button etraffic = findViewById(R.id.btn_etraffic);
        Button econtrol = findViewById(R.id.btn_econtrol);
        Button elapsus = findViewById(R.id.btn_elapsus);
        Button gantipin = findViewById(R.id.btn_gantipin);
        Button eimei = findViewById(R.id.btn_eimei);
        btnlogout.setOnClickListener(v -> confirmRekapLogout());
        emergencycall.setOnClickListener(v -> {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                onGPS();
            } else {
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION_CODE);
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_COARSE_LOCATION_CODE);
                confirmEmergency();
            }
        });
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

    private void onGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Nyalakan dulu GPS nya").setCancelable(false)
                .setPositiveButton("Oke", (dialog, which) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("Tidak", (dialog, which) -> { dialog.cancel(); finish();
                });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(Welcome.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(Welcome.this,
                    new String[] { permission },
                    requestCode);
        }
    }

    private void confirmEmergency(){
        if (ActivityCompat.checkSelfPermission(
                Welcome.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                Welcome.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_CODE);
        } else {
            FusedLocationProviderClient mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocation.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null){
                    double lat = location.getLatitude();
                    double longi = location.getLongitude();
                    final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Welcome.this);
                    builder.setTitle("Kirim Panggilan Darurat ?");
                    builder.setMessage("Pemberitahuan akan dikirim ke seluruh petugas dan pimpinan.");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Iya, Kirim", (dialog, which) -> sendEmergencyCall(lat,longi));
                    builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
                    builder.show();
                }else {
                    showDialogGagalGPS();
                }
            });
        }
    }

    private void sendEmergencyCall(double lat, double longi){
        JSONObject json = new JSONObject();
        try {
            String nip = new PrefManager(this).getNip();
            String nama = new PrefManager(this).getNama();
            String regu = new PrefManager(this).getRegu();
            String foto = new PrefManager(this).getFoto();

            json.put("to","/topics/"+"umjoL1srorNjDvpmeocBJ1kN7pVTb4t9zgmsPCHIs");
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", "Laporan Masuk !");
            notificationObj.put("body", "PERINGATAM KEADAAN DARURAT OLEH "+nama.toUpperCase());

            JSONObject extraData = new JSONObject();
            extraData.put("senderid",nip);
            extraData.put("messagetype","emergency");
            extraData.put("sendername",nama);
            extraData.put("senderregu",regu);
            extraData.put("senderphoto",foto);
            extraData.put("senderlocation_lat",lat);
            extraData.put("senderlocation_longi",longi);

            json.put("notification",notificationObj);
            json.put("data",extraData);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, SENDNOTIFURL,
                    json,
                    response -> showSentECDialog(), error -> Log.d("MUR", "onError: "+error.networkResponse)
            ){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AAAA9qTy6zA:APA91bH8PJ5l5GfbXpr4XUErtv-Bbkc08ok3hotwKi1P39XCJWkCBtWAUqq5Q0gzaSguASf0LpODm77J5lz-K_MV6__DwDnQ_1Y0dgWFnS_-plT5ie8tU3rOaYcnNzbCNkMVYdGU0_dr");
                    return header;
                }
            };
            mRequestQue.add(request);
        }
        catch (JSONException e)

        {
            e.printStackTrace();
        }

    }

    private void showSentECDialog(){
            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Welcome.this);
            builder.setTitle("Panggilan Darurat Tekirim");
            builder.setMessage("Rekan lain akan segera menghubungi Anda.");
            builder.setCancelable(false);
            builder.setPositiveButton("Oke", (dialog, which) -> dialog.dismiss());
            builder.show();
    }

    private void showDialogGagalGPS(){
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Welcome.this);
        builder.setTitle("Gagal Mendapatkan Lokasi");
        builder.setMessage("Coba Kalibrasi GPS dengan aplikasi Google Maps, lalu ulangi proses ini.");
        builder.setCancelable(true);
        builder.setPositiveButton("Oke", (dialog, which) -> dialog.dismiss());
        builder.show();
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