package pendataan.parkir.kedungpane;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import id.zelory.compressor.Compressor;

public class MainActivity3 extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference folderstorage;
    private EditText namapelapor, isilaporan;
    private BlurView blurbgform;
    private ProgressDialog progressDialog;
    private ImageView fotolaporan;
    private Uri datafotolaporan, compresseddatafotolaporan;
    private String takenPhotoPath = null;
    private static final int STORAGE_PERMISSION_CODE = 1010;
    private static final int CAMERA_PERMISSION_CODE = 1011;
    private static final int TAKE_CAMERA_FOTO = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        folderstorage = FirebaseStorage.getInstance().getReference();
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
            if(TextUtils.isEmpty(namapelapor.getText().toString())|TextUtils.isEmpty(isilaporan.getText().toString())){
                Toast.makeText(getApplication(), "Mohon Isi Seluruh Kolom.", Toast.LENGTH_SHORT).show();
            }else{
                confirmKirim();
            }
        });
        back.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity3.this, Welcome.class);
            startActivity(intent);
            finish();
        });
        blurinForm();
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
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void pickFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                Uri photoURI = FileProvider.getUriForFile(this, this.getPackageName()+".camprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_CAMERA_FOTO);
            } catch (Exception ex) {
                Toast.makeText(getApplication(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }else
        {
            Toast.makeText(getApplication(), "Foto kosong.", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String randomName = GenerateRandomValue.getId(5);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(randomName,".jpg", storageDir);
        takenPhotoPath = image.getAbsolutePath();
        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if(requestCode == TAKE_CAMERA_FOTO) {
            if(resultCode == RESULT_OK){
                compresseddatafotolaporan = Uri.fromFile(Compressor.getDefault(this).compressToFile(new File(takenPhotoPath)));
                datafotolaporan = Uri.fromFile(new File(takenPhotoPath));
                fotolaporan.setImageURI(datafotolaporan);
            }
        }
    }

    private void uploadFoto(){
        final StorageReference lokasifoto = folderstorage.child("fotowasrik").child("lapsus").child(namapelapor.getText().toString()+"_"+datafotolaporan.getLastPathSegment());
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
        Map<String, Object> docData = new HashMap<>();
        docData.put("isilaporan", String.valueOf(isilaporan.getText()));
        docData.put("namapelapor", String.valueOf(namapelapor.getText()));
        docData.put("foto", foto);
        docData.put("tgllaporan", new Date());
        db.collection("lapsus")
                .document().set(docData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity3.this,"Data berhasil disimpan.",Toast.LENGTH_LONG).show();finish();
                }).addOnFailureListener(e -> Toast.makeText(MainActivity3.this,"Gagal menambahkan data! Periksa koneksi.",Toast.LENGTH_LONG).show());
    }

}