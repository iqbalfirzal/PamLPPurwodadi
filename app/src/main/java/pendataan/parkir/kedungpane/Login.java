package pendataan.parkir.kedungpane;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class Login extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private EditText usernamenya, passwordnya;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        usernamenya = findViewById(R.id.username);
        passwordnya = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        Button masuk = findViewById(R.id.btn_login);
        masuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernamenya.getText().toString() ;
                final String password = passwordnya.getText().toString() ;
                final String[] userEmail = new String[1];
                final String[] userRegu = new String[1];
                progressBar.setVisibility(View.VISIBLE);
                if(username.length() > 0 && password.length() > 0){
                    Query loginguser = db.collection("user").whereEqualTo("username",username);
                    loginguser.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    userEmail[0] =  Objects.requireNonNull(document.getString("email"));
                                    userRegu[0] =  Objects.requireNonNull(document.getString("regu"));
                                    if(userRegu != null){
                                        performLogin(userEmail[0], username, password, userRegu[0]);
                                    }
                                }
                            }
                        }}).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, "Login gagal. Mohon periksa koneksi.", Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Username atau Password kosong!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void performLogin(final String emailId, String username, String password, String regu) {
            auth.signInWithEmailAndPassword(emailId,password).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressBar.setVisibility(View.GONE);
                    if(!task.isSuccessful()){
                        Toast.makeText(Login.this, "Password salah.",
                                Toast.LENGTH_LONG).show();
                    }else{
                        saveLoginDetails(emailId, username, password, regu);
                        Intent intent = new Intent(Login.this, Welcome.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
    }

    private void saveLoginDetails(String email, String username, String password, String regu){
        new PrefManager(this).saveLoginDetails(email, username, password, regu);
    }

}