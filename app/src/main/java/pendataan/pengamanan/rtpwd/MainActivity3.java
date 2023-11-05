package pendataan.pengamanan.rtpwd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import id.zelory.compressor.Compressor;

public class MainActivity3 extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference dbRef = db.collection("lapsus");
    private StorageReference folderstorage;
    private RequestQueue mRequestQue;
    private final String SENDNOTIFURL = "https://fcm.googleapis.com/fcm/send";
    private EditText namapelapor, isilaporan;
    private BlurView blurbgform;
    private ProgressDialog progressDialog;
    private ImageView fotolaporan;
    private Uri datafotolaporan, compresseddatafotolaporan;
    private String takenPhotoPath = null;
    private static final int STORAGE_PERMISSION_CODE = 1010;
    private static final int CAMERA_PERMISSION_CODE = 1011;
    private static final int REQUEST_FINE_LOCATION_CODE = 1111;
    private static final int REQUEST_COARSE_LOCATION_CODE = 1101;
    private LocationManager locationManager;
    private LocationResolver mLocationResolver;
    private GeoPoint lokasilaporankhusus;
    private ListLapsusAdapter adapter;
    private RecyclerView recyclerView;
    private Date ldate,gdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        folderstorage = FirebaseStorage.getInstance().getReference();
        mLocationResolver = new LocationResolver(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mRequestQue = Volley.newRequestQueue(this);
        LinearLayout form = findViewById(R.id.laporannya);
        LinearLayout tambahfoto = findViewById(R.id.tambahfoto);
        blurbgform = findViewById(R.id.formtambahlapsus);
        namapelapor = findViewById(R.id.namapelapor);
        namapelapor.setText(new PrefManager(this).getNama());
        isilaporan = findViewById(R.id.isilaporan);
        Button addfoto = findViewById(R.id.btn_tambahfoto);
        Button backfoto = findViewById(R.id.btn_back_kirimfoto);
        addfoto.setOnClickListener(v -> {
            checkPermission(Manifest.permission.CAMERA, STORAGE_PERMISSION_CODE);
            form.setVisibility(View.GONE);
            tambahfoto.setVisibility(View.VISIBLE);
        });
        Button kirim = findViewById(R.id.btn_kirimlaporan);
        ImageButton back = findViewById(R.id.btn_back_addlaporan);
        fotolaporan = findViewById(R.id.fotolaporan);
        fotolaporan.setOnClickListener(v -> {
            checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
            pickFoto();
        });
        backfoto.setOnClickListener(v -> {
            form.setVisibility(View.VISIBLE);
            tambahfoto.setVisibility(View.GONE);
        });
        kirim.setOnClickListener(v -> {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                onGPS();
            } else {
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION_CODE);
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_COARSE_LOCATION_CODE);
                if(TextUtils.isEmpty(namapelapor.getText().toString())|TextUtils.isEmpty(isilaporan.getText().toString())){
                    Toast.makeText(getApplication(), "Mohon Isi Seluruh Kolom.", Toast.LENGTH_SHORT).show();
                }else{
                    confirmKirim();
                }
            }
        });
        back.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity3.this, Welcome.class);
            startActivity(intent);
            finish();
        });
        recyclerView = findViewById(R.id.rvlaporan);
        blurinForm();
        setUpRecyclerView();
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
        if (ContextCompat.checkSelfPermission(MainActivity3.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(MainActivity3.this,
                    new String[] { permission },
                    requestCode);
        }
    }

    private void confirmKirim(){
        if (ActivityCompat.checkSelfPermission(
                MainActivity3.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                MainActivity3.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_CODE);
        } else {
            mLocationResolver.resolveLocation(this, location -> {
                if (location != null){
                    double lat = location.getLatitude();double longi = location.getLongitude();
                    lokasilaporankhusus = new GeoPoint(lat,longi);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Kirim Laporan ?");
                    builder.setMessage("Ingat untuk tidak mengirim laporan yang salah.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Kirim", (dialog, id) -> {
                        progressDialog = new ProgressDialog(MainActivity3.this);
                        progressDialog.isIndeterminate();
                        progressDialog.setMessage("Sedang mengunggah data...");
                        progressDialog.setTitle("Laporan Khusus");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        if(datafotolaporan == null){
                            kirimData("");
                        }else{
                            uploadFoto();
                        }
                    });
                    builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
                    builder.show();
                }else {
                    Toast.makeText(MainActivity3.this, "Tidak dapat menemukan lokasi, coba lagi.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void pickFoto() {
        try {
            File photoFile = createImageFile();
            Uri photoURI = FileProvider.getUriForFile(this, this.getPackageName()+".camprovider", photoFile);
            takePictureLapsus.launch(photoURI);
        } catch (Exception ex) {
            Toast.makeText(getApplication(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    ActivityResultLauncher<Uri> takePictureLapsus = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result){
                        compresseddatafotolaporan = Uri.fromFile(Compressor.getDefault(getApplicationContext()).compressToFile(new File(takenPhotoPath)));
                        datafotolaporan = Uri.fromFile(new File(takenPhotoPath));
                        fotolaporan.setImageURI(datafotolaporan);
                    }else{
                        Toast.makeText(getApplication(), "Gagal mengambil gambar.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private File createImageFile() throws IOException {
        String randomName = GenerateRandomValue.getId(5);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(randomName,".jpg", storageDir);
        takenPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void uploadFoto(){
        final StorageReference lokasifoto = folderstorage.child("fotopengamanan").child("lapsus").child(namapelapor.getText().toString()+"_"+datafotolaporan.getLastPathSegment());
        lokasifoto.putFile(compresseddatafotolaporan).continueWithTask(task -> {
            if (!task.isSuccessful()){
                throw Objects.requireNonNull(task.getException());
            }
            return lokasifoto.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Uri downUri = task.getResult();
                fotolaporan.setImageResource(R.drawable.ic_camera);
                assert downUri != null;
                deleteTempFiles(downUri.toString());
            }
        });
    }

    private void deleteTempFiles(String downurl) {
        File delfoto = new File(datafotolaporan.getPath());
        if (delfoto.delete()) {
            kirimData(downurl);
        } else {
            kirimData(downurl);
        }
    }

    private void kirimData(String foto){
        String fotopelapor = new PrefManager(this).getFoto();
        String nippelapor = new PrefManager(this).getNip();
        Map<String, Object> docData = new HashMap<>();
        docData.put("isilaporan", String.valueOf(isilaporan.getText()));
        docData.put("namapelapor", String.valueOf(namapelapor.getText()));
        docData.put("nippelapor", nippelapor);
        docData.put("foto", foto);
        docData.put("fotopelapor", fotopelapor);
        docData.put("geo", lokasilaporankhusus);
        docData.put("instruksipim", "");
        docData.put("tgllaporan", new Date());
        DocumentReference ref = db.collection("lapsus").document();
        String refId = ref.getId();
        ref.set(docData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity3.this,"Data berhasil disimpan.",Toast.LENGTH_LONG).show();sendNotifLapsus(foto, refId, fotopelapor);
                }).addOnFailureListener(e -> Toast.makeText(MainActivity3.this,"Gagal menambahkan data! Periksa koneksi.",Toast.LENGTH_LONG).show());
    }

    private void sendNotifLapsus(String foto, String id, String fotopelapor){
        JSONObject json = new JSONObject();
        try {
            String nip = new PrefManager(this).getNip();
            String nama = namapelapor.getText().toString();
            String pesan = isilaporan.getText().toString();
            double lati = lokasilaporankhusus.getLatitude();double longit = lokasilaporankhusus.getLongitude();

            json.put("to","/topics/"+"umjoL1srorNjDvpmeocBJ1kN7pVTb4t9zgmsPCHIs");
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", "Laporan Khusus Masuk Oleh : "+nama);
            notificationObj.put("body", pesan);
            notificationObj.put("click_action", "BUKA_ACTIVITY_LAPSUS");

            JSONObject extraData = new JSONObject();
            extraData.put("docId", id);
            extraData.put("senderid", nip);
            extraData.put("messagetype","lapsus");
            extraData.put("isilaporan", pesan);
            extraData.put("namapelapor", nama);
            extraData.put("foto", foto);
            extraData.put("fotopelapor", fotopelapor);
            extraData.put("senderlocation_lat", lati);
            extraData.put("senderlocation_longi", longit);
            extraData.put("instruksipim", "");
            extraData.put("tgllaporan", formatTanggal(new Date()));

            json.put("notification",notificationObj);
            json.put("data",extraData);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, SENDNOTIFURL,
                    json,
                    response -> finish(), error -> Log.d("LAPSUS SEND NOTIF ERROR", "onError: "+error.networkResponse)
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
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUpRecyclerView(){
        Date today = new Date();
        SimpleDateFormat todayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateFormated = todayDateFormat.format(today);
        String lessDate = dateFormated+" 23:59:59";
        String greatDate = dateFormated+" 00:00:01";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.getDefault());
        try{
            ldate = dateFormat.parse(lessDate);
            gdate = dateFormat.parse(greatDate);
        } catch (ParseException e){
            e.printStackTrace();
        }assert ldate != null;assert gdate != null;
        Query query = dbRef.orderBy("tgllaporan")
                .whereGreaterThan("tgllaporan", new Timestamp(gdate))
                .whereLessThan("tgllaporan", new Timestamp(ldate)).whereEqualTo("namapelapor", new PrefManager(this).getNama());
        FirestoreRecyclerOptions<LapsusModel> options = new FirestoreRecyclerOptions.Builder<LapsusModel>().setQuery(query, LapsusModel.class).build();
        adapter = new ListLapsusAdapter(options,this );
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private String formatTanggal(Date tanggal){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatTanggal = new SimpleDateFormat("EEEE, dd MMMM yyy");
        return formatTanggal.format(tanggal);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
        mLocationResolver.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
        mLocationResolver.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationResolver.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationResolver.onRequestPermissionsResult(requestCode, grantResults);
    }

}