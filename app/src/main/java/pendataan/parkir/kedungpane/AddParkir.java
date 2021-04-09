package pendataan.parkir.kedungpane;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import id.zelory.compressor.Compressor;

public class AddParkir extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference folderstorage;
    private ProgressDialog progressDialog;
    private BlurView blurbgform;
    private EditText plat,keperluan;
    private Spinner jk;
    private ImageView fotokendaraan;
    private Uri datafotokendaraan, compresseddatafotokendaraan;
    private String takenPhotoPath = null;
    private static final int STORAGE_PERMISSION_CODE = 1010;
    private static final int CAMERA_PERMISSION_CODE = 1011;
    private static final int TAKE_CAMERA_FOTO = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parkir);
        folderstorage = FirebaseStorage.getInstance().getReference();
        LinearLayout form = findViewById(R.id.identitaskendaraan);
        LinearLayout tambahfoto = findViewById(R.id.tambahfoto);
        blurbgform = findViewById(R.id.formtambahkendaraan);
        plat = findViewById(R.id.platnomor);
        keperluan = findViewById(R.id.keperluan);
        jk = findViewById(R.id.jeniskendaraan_opt);
        Button addfoto = findViewById(R.id.btn_tambahfoto);
        Button addparkir = findViewById(R.id.btn_daftarparkir);
        Button backfoto = findViewById(R.id.btn_back_kirimfoto);
        fotokendaraan = findViewById(R.id.fotokendaraan);
        ImageButton backaddparkir = findViewById(R.id.btn_back_addparkir);
        setUpSpinnerJK();
        addfoto.setOnClickListener(v -> {
            checkPermission(Manifest.permission.CAMERA, STORAGE_PERMISSION_CODE);
            form.setVisibility(View.GONE);
            tambahfoto.setVisibility(View.VISIBLE);
        });
        addparkir.setOnClickListener(v -> {
            if(TextUtils.isEmpty(plat.getText().toString())|jk.getSelectedItem()==null){
                Toast.makeText(getApplication(), "Mohon Isi Plat Nomor.", Toast.LENGTH_SHORT).show();
            }else{
                progressDialog = new ProgressDialog(AddParkir.this);
                progressDialog.isIndeterminate();
                progressDialog.setMessage("Sedang mengunggah data...");
                progressDialog.setTitle("Kendaraan Masuk");
                progressDialog.setCancelable(false);
                progressDialog.show();
                if(datafotokendaraan == null){
                    addDataParkir("");
                }else{
                    uploadFoto();
                }
            }
        });
        backfoto.setOnClickListener(v -> {
            form.setVisibility(View.VISIBLE);
            tambahfoto.setVisibility(View.GONE);
        });
        fotokendaraan.setOnClickListener(v -> {
            checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
            pickFoto();
        });
        backaddparkir.setOnClickListener(v -> {
            Intent intent = new Intent(AddParkir.this, MainActivity.class);
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

    private void setUpSpinnerJK() {
        List<String> list = new ArrayList<>();
        list.add("Motor");list.add("Mobil");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jk.setAdapter(dataAdapter);
    }

    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(AddParkir.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(AddParkir.this,
                    new String[] { permission },
                    requestCode);
        }
        else {        }
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
                compresseddatafotokendaraan = Uri.fromFile(Compressor.getDefault(this).compressToFile(new File(takenPhotoPath)));
                datafotokendaraan = Uri.fromFile(new File(takenPhotoPath));
                fotokendaraan.setImageURI(datafotokendaraan);
            }
        }
    }

    private void uploadFoto(){
        final StorageReference lokasifoto = folderstorage.child("fotowasrik").child("traffic").child(plat.getText().toString()+"_"+datafotokendaraan.getLastPathSegment());
        lokasifoto.putFile(compresseddatafotokendaraan).continueWithTask(task -> {
            if (!task.isSuccessful()){
                throw task.getException();
            }
            return lokasifoto.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Uri downUri = task.getResult();
                fotokendaraan.setImageResource(R.drawable.ic_camera);
                deleteTempFiles(downUri.toString());
            }
        });
    }

    private void deleteTempFiles(String downurl) {
        File delfoto = new File(datafotokendaraan.getPath());
        if (delfoto.delete()) {
            addDataParkir(downurl);
        } else {
            addDataParkir(downurl);
        }
    }

    private void addDataParkir(String foto){
        Map<String, Object> docData = new HashMap<>();
        docData.put("foto", foto);
        docData.put("jeniskendaraan", jk.getSelectedItem().toString());
        docData.put("keluarjam", new Date());
        docData.put("keperluan", String.valueOf(keperluan.getText()));
        docData.put("masukjam", new Date());
        docData.put("platnomor", String.valueOf(plat.getText()).toUpperCase());
        docData.put("sudahkeluar", false);
        db.collection("parkir").document()
                .set(docData)
                .addOnSuccessListener(aVoid -> {
                    plat.setText("");keperluan.setText("");takenPhotoPath = null;datafotokendaraan = null;
                    progressDialog.dismiss();
                    Toast.makeText(AddParkir.this,"Data berhasil disimpan",Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> Toast.makeText(AddParkir.this,"Gagal menambahkan data! Periksa koneksi.",Toast.LENGTH_LONG).show());
    }

}