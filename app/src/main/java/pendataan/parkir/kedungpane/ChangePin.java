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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import id.zelory.compressor.Compressor;

public class ChangePin extends AppCompatActivity {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private BlurView blurbgform;
    private EditText namapengguna, pinpengguna;
    private Spinner reguopt;
    private ProgressDialog progressDialog;
    private CircularImageView fotoakun;
    private Uri datafotoakun, compresseddatafotoakun;
    private File photoFile = null;
    private String takenPhotoPath;
    private StorageReference folderstorage;
    private static final int STORAGE_PERMISSION_CODE = 1010;
    private static final int CAMERA_PERMISSION_CODE = 1011;
    private static final int TAKE_CAMERA_FOTO_AKUN = 101;
    private static final int LOAD_FROM_GALLERY_FOTO_AKUN = 111;
    private String linkfoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);
        folderstorage = FirebaseStorage.getInstance().getReference();
        blurbgform =  findViewById(R.id.formeditpin);
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        fotoakun = findViewById(R.id.fotoprofil);
        namapengguna = findViewById(R.id.namapengguna);
        pinpengguna = findViewById(R.id.pinpengguna);
        reguopt = findViewById(R.id.regu_opt);
        Button gantipin = findViewById(R.id.btn_gantipin);
        ImageButton back = findViewById(R.id.btn_back_changepin);
        gantipin.setOnClickListener(v -> {
            if(TextUtils.isEmpty(namapengguna.getText().toString())
                    |TextUtils.isEmpty(pinpengguna.getText().toString())|reguopt.getSelectedItem()==null){
                Toast.makeText(getApplication(), "Masih ada kolom yang kosong.", Toast.LENGTH_SHORT).show();
            }else{
                confirmGantiPin();
            }
        });
        back.setOnClickListener(v -> finish());
        fotoakun.setOnClickListener(v -> {
            checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
            gantiFoto();
        });
        blurinForm();
        setDataSemula();
        setUpSpinnerRegu();
        setUpProgressDialog();
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
        if (ContextCompat.checkSelfPermission(ChangePin.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(ChangePin.this,
                    new String[] { permission },
                    requestCode);
        }
    }

    private void setDataSemula(){
        namapengguna.setText(new PrefManager(this).getNama());
        pinpengguna.setText(new PrefManager(this).getPin());
        if(new PrefManager(this).getFoto().equals("")){
            fotoakun.setImageResource(R.drawable.ic_account);
        }else{
            Glide.with(this)
                    .load(new PrefManager(this).getFoto())
                    .placeholder(R.drawable.ic_account)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop()
                    .into(fotoakun);
        }
    }

    private void setUpSpinnerRegu() {
        List<String> list = new ArrayList<>();
        list.add("REGU 1");list.add("REGU 2");list.add("REGU 3");list.add("REGU 4");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reguopt.setAdapter(dataAdapter);
        String selectedList = new PrefManager(this).getRegu();
        for(int i=0; i < dataAdapter.getCount(); i++) {
            if (selectedList.trim().equals(dataAdapter.getItem(i))) {
                reguopt.setSelection(i);
                break;
            }
        }
    }

    private void setUpProgressDialog(){
        progressDialog = new ProgressDialog(ChangePin.this);
        progressDialog.isIndeterminate();
        progressDialog.setMessage("Sedang mengunggah data...");
        progressDialog.setTitle("Perbarui Data Akun");
        progressDialog.setCancelable(false);
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void gantiFoto() {
        final CharSequence[] options = { "Kamera", "Galeri","Batal" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ChangePin.this);
        builder.setTitle("Ganti Foto Profil");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Kamera"))
            {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    try {
                        photoFile = createImageFile();
                        Uri photoURI = FileProvider.getUriForFile(this, this.getPackageName()+".camprovider", photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, TAKE_CAMERA_FOTO_AKUN);
                    } catch (Exception ex) {
                        Toast.makeText(getApplication(), ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }else
                {
                    Toast.makeText(getApplication(), "Foto kosong.", Toast.LENGTH_SHORT).show();
                }
            }
            else if (options[item].equals("Galeri"))
            {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , LOAD_FROM_GALLERY_FOTO_AKUN);
            }
            else if (options[item].equals("Batal")) {
                dialog.dismiss();
            }
        });
        builder.show();
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
        switch(requestCode) {
            case 101:
                if(resultCode == RESULT_OK){
                    compresseddatafotoakun = Uri.fromFile(Compressor.getDefault(this).compressToFile(new File(takenPhotoPath)));
                    datafotoakun = Uri.fromFile(new File(takenPhotoPath));
                    fotoakun.setImageURI(datafotoakun);
                    uploadFoto();
                }
                break;
            case 111:
                if(resultCode == RESULT_OK && imageReturnedIntent != null){
                    Uri selectedImage = imageReturnedIntent.getData();
                    datafotoakun = imageReturnedIntent.getData();
                    compresseddatafotoakun = imageReturnedIntent.getData();
                    fotoakun.setImageURI(selectedImage);
                    uploadFoto();
                }
                break;
        }
    }

    private void uploadFoto(){
        progressDialog.show();
        final StorageReference lokasifotoktp = folderstorage.child("fotowasrik").child("profil").child("FP_NIP_"+new PrefManager(this).getNip());
        lokasifotoktp.putFile(compresseddatafotoakun).addOnSuccessListener(taskSnapshot -> lokasifotoktp.getDownloadUrl().addOnSuccessListener(this::deleteTempFiles)).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(ChangePin.this,"Gagal mengunggah! Periksa koneksi.",Toast.LENGTH_LONG).show();
        });
    }

    private void deleteTempFiles(final Uri urifotoakun) {
        File delfotodiri = new File(datafotoakun.getPath());
        File delfotoktp = new File(datafotoakun.getPath());
        if (delfotodiri.delete()&&delfotoktp.delete()) {
            UpdateDataFoto(urifotoakun);
        } else {
            UpdateDataFoto(urifotoakun);
        }
    }

    private void UpdateDataFoto(final Uri urifotoakun){
        Map<String, Object> docData = new HashMap<>();
        docData.put("nama", new PrefManager(this).getNama());
        docData.put("pin", new PrefManager(this).getPin());
        docData.put("regu", new PrefManager(this).getRegu());
        docData.put("foto", String.valueOf(urifotoakun));
        db.collection("petugas").document(new PrefManager(this).getNip())
                .set(docData)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    linkfoto = String.valueOf(urifotoakun);
                    new PrefManager(this).updateFoto(String.valueOf(urifotoakun));
                }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(ChangePin.this,"Gagal menambahkan data! Periksa koneksi.",Toast.LENGTH_LONG).show();
        });
    }

    private void confirmGantiPin(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Perbarui Data Akun");
        builder.setMessage("Pastikan seluruh data sudah benar?");
        builder.setCancelable(false);
        builder.setPositiveButton("Iya", (dialog, which) -> {
            progressDialog.show();
            kirimData();
        });
        builder.setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void kirimData(){
        Map<String, Object> docData = new HashMap<>();
        docData.put("nama", String.valueOf(namapengguna.getText()));
        docData.put("pin", String.valueOf(pinpengguna.getText()));
        docData.put("regu", reguopt.getSelectedItem().toString());
        docData.put("foto", new PrefManager(this).getFoto());
        db.collection("petugas").document(new PrefManager(this).getNip())
                .set(docData)
                .addOnSuccessListener(aVoid -> {
                    new PrefManager(this).updateLoginDetails(String.valueOf(namapengguna.getText()), String.valueOf(pinpengguna.getText()), reguopt.getSelectedItem().toString());progressDialog.dismiss();
                    Toast.makeText(ChangePin.this,"Data berhasil diperbarui.",Toast.LENGTH_LONG).show();finish();})
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(ChangePin.this,"Gagal menambahkan data! Periksa koneksi.",Toast.LENGTH_LONG).show();
                });
    }

}