package pendataan.pengamanan.rtpwd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class MainActivity2 extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference folderstorage;
    private ProgressDialog progressDialog;
    private EditText namapetugas, regu, keterangan;
    private ImageView fotopetugas;
    private Uri datafotopetugas;
    private String takenPhotoPath = null;
    private static final int STORAGE_PERMISSION_CODE = 1010;
    private static final int CAMERA_PERMISSION_CODE = 1011;
    private static final int REQUEST_FINE_LOCATION_CODE = 1111;
    private static final int REQUEST_COARSE_LOCATION_CODE = 1101;
    private LocationManager locationManager;
    private double currentlat=0; private double currentlongi=0;
    private LocationResolver mLocationResolver;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        folderstorage = FirebaseStorage.getInstance().getReference();
        namapetugas = findViewById(R.id.namapetugas);
        namapetugas.setText(new PrefManager(this).getNama());
        regu = findViewById(R.id.regu);
        keterangan = findViewById(R.id.keterangan);
        Button scan = findViewById(R.id.btn_scan);
        Button tambahfoto = findViewById(R.id.btn_tambahfotolaporan);
        ImageView fotoilustrasi = findViewById(R.id.fotoilustrasi);
        ImageView back = findViewById(R.id.btn_back_control);
        fotopetugas = findViewById(R.id.fotopetugas);
        mLocationResolver = new LocationResolver(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LinearLayout laytambahfoto = findViewById(R.id.layfotopetugas);
        tambahfoto.setOnClickListener(v -> {
            checkPermission(Manifest.permission.CAMERA, STORAGE_PERMISSION_CODE);
            if(fotoilustrasi.getVisibility()==View.VISIBLE){
                fotoilustrasi.setVisibility(View.GONE);
                laytambahfoto.setVisibility(View.VISIBLE);
                tambahfoto.setText("OKE");
            }else{
                fotoilustrasi.setVisibility(View.VISIBLE);
                laytambahfoto.setVisibility(View.GONE);
                tambahfoto.setText("TAMBAH FOTO (OPTIONAL)");
            }
        });
        scan.setOnClickListener(v -> {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                onGPS();
            } else {
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_FINE_LOCATION_CODE);
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_COARSE_LOCATION_CODE);
                if (TextUtils.isEmpty(namapetugas.getText().toString()) | TextUtils.isEmpty(keterangan.getText().toString()) | TextUtils.isEmpty(regu.getText().toString())) {
                    Toast.makeText(getApplication(), "Masih ada kolom yang kosong.", Toast.LENGTH_SHORT).show();
                } else {
                    if (ActivityCompat.checkSelfPermission(
                            MainActivity2.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            MainActivity2.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_CODE);
                    } else {
                        mLocationResolver.resolveLocation(this, location -> {
                            if (location != null){
                                currentlat = location.getLatitude();
                                currentlongi = location.getLongitude();
                                new Handler().postDelayed(this::exeScan, 1000 );
                            }else {
                                showDialogGagalGPS();
                            }
                        });
                    }
                }
            }
        });
        fotopetugas.setOnClickListener(v -> {
            checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
            pickFoto();
        });
        back.setOnClickListener(v -> finish());
        setUpRegu();
    }

    private void setUpRegu() {
        regu.setText(new PrefManager(this).getRegu());
        regu.setEnabled(false);
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
        if (ContextCompat.checkSelfPermission(MainActivity2.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(MainActivity2.this,
                    new String[] { permission },
                    requestCode);
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void pickFoto() {
        try {
            File photoFile = createImageFile();
            Uri photoURI = FileProvider.getUriForFile(this, this.getPackageName()+".camprovider", photoFile);
            takePictureControl.launch(photoURI);
        } catch (Exception ex) {
            Toast.makeText(getApplication(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String randomName = GenerateRandomValue.getId(5);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(randomName,".jpg", storageDir);
        takenPhotoPath = image.getAbsolutePath();
        return image;
    }

    ActivityResultLauncher<Uri> takePictureControl = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result){
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy HH:mm");
                        try {
                            Bitmap bahaneditfoto = MediaStore.Images.Media.getBitmap(MainActivity2.this.getContentResolver(), Uri.fromFile(new File(takenPhotoPath)));
                            Bitmap editeddatafoto = mark(bahaneditfoto, sdf.format(new Date()));
                            FileOutputStream fos = new FileOutputStream(takenPhotoPath);
                            editeddatafoto.compress(Bitmap.CompressFormat.JPEG, 10, fos);
                            fotopetugas.setImageBitmap(editeddatafoto);
                            datafotopetugas = Uri.fromFile(new File(takenPhotoPath));
                            fos.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }else{
                        Toast.makeText(getApplication(), "Gagal mengambil gambar.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private static Bitmap mark(Bitmap src, String watermark) {
        int w = src.getWidth();
        int h = src.getHeight();
        Matrix matrix = new Matrix();
        matrix.preRotate(90);
        Bitmap result = Bitmap.createBitmap(src , 0, 0, w, h, matrix, true);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(result, 0, 0, null);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(150);
        paint.setShadowLayer(20, 0, 0, Color.GRAY);
        canvas.drawText(watermark, 300, 300, paint);
        return result;
    }

    private void exeScan(){
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan QR Kontrol Dengan Posisi HP Tegak");
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Dibatalkan, silahkan coba lagi.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject obj = new JSONObject(intentResult.getContents());
                    String namalp = (obj.getString("namalp"));
                    String namacekpoin = (obj.getString("namacekpoin"));
                    dapatkanDataLokasi(namalp,namacekpoin);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, intentResult.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void dapatkanDataLokasi(String namalp, String namacekpoin){
        if(namalp.equals("rtpwd")){
            confirmKirimData(namacekpoin,currentlat,currentlongi);
        }else{
            Toast.makeText(MainActivity2.this,"Scan QR Code yang telah ditentukan.",Toast.LENGTH_LONG).show();
        }
    }

    private void confirmKirimData(String namacekpoin, double lat, double longi){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kirim Laporan Kontrol ?");
        builder.setMessage("Nama Petugas : "+namapetugas.getText().toString()
                +"\nRegu : "+regu.getText().toString()+"\nPos Kontrol : "+namacekpoin+"\nKeterangan : "+keterangan.getText().toString()+"\nTitik Koordinat Kontrol : "+ new GeoPoint(lat, longi));
        builder.setCancelable(false);
        builder.setPositiveButton("Kirim", (dialog, which) -> kirimData(namacekpoin,lat,longi));
        builder.setNegativeButton("Batal", (dialog, which) -> {dialog.dismiss(); currentlat=0;currentlongi=0;});
        builder.show();
    }

    private void kirimData(String namacekpoin, double lat, double longi){
        progressDialog = new ProgressDialog(MainActivity2.this);
        progressDialog.isIndeterminate();
        progressDialog.setMessage("Sedang mengunggah data...");
        progressDialog.setTitle("Laporan Kontrol Pengamanan");
        progressDialog.setCancelable(false);
        progressDialog.show();
        if(datafotopetugas==null){
            exeKirimData(namacekpoin, "",lat,longi);
        }else{
            final StorageReference lokasifoto = folderstorage.child("fotopengamanan").child("control").child(namapetugas.getText().toString()+"_"+datafotopetugas.getLastPathSegment());
            lokasifoto.putFile(datafotopetugas).continueWithTask(task -> {
                if (!task.isSuccessful()){
                    throw Objects.requireNonNull(task.getException());
                }
                return lokasifoto.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Uri downUri = task.getResult();
                    fotopetugas.setImageResource(R.drawable.ic_camera);
                    assert downUri != null;
                    deleteTempFiles(namacekpoin, downUri.toString(),lat,longi);
                }
            });
        }
    }

    private void deleteTempFiles(String cekpoin, String downurl, double lat, double longi) {
        File delfoto = new File(Objects.requireNonNull(datafotopetugas.getPath()));
        if (delfoto.delete()) {
            exeKirimData(cekpoin, downurl,lat,longi);
        } else {
            exeKirimData(cekpoin, downurl,lat,longi);
        }
    }

    private void exeKirimData(String namacekpoin, String foto, double lat, double longi){
        Map<String, Object> docData = new HashMap<>();
        docData.put("foto", foto);
        docData.put("jamkontrol", new Date());
        docData.put("petugas", String.valueOf(namapetugas.getText()));
        docData.put("poskontrol", namacekpoin);
        docData.put("regu", String.valueOf(regu.getText()));
        docData.put("geo", new GeoPoint(lat,longi));
        docData.put("keterangan", String.valueOf(keterangan.getText()));
        db.collection("kontrolpengamanan")
                .document().set(docData)
                .addOnSuccessListener(aVoid -> {
                    currentlat = 0;currentlongi = 0;takenPhotoPath = null;datafotopetugas = null;progressDialog.dismiss();
                    Toast.makeText(MainActivity2.this,"Laporan terkirim.",Toast.LENGTH_LONG).show();finish();
                }).addOnFailureListener(e -> Toast.makeText(MainActivity2.this,"Gagal menambahkan data! Periksa koneksi.",Toast.LENGTH_LONG).show());
    }

    private void showDialogGagalGPS(){
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity2.this);
        builder.setTitle("Gagal Mendapatkan Lokasi");
        builder.setMessage("Coba Kalibrasi GPS dengan aplikasi Google Maps, lalu ulangi proses ini.");
        builder.setCancelable(true);
        builder.setPositiveButton("Oke", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationResolver.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
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