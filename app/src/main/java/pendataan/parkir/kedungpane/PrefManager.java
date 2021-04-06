package pendataan.parkir.kedungpane;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private Context context;

    PrefManager(Context context) {
        this.context = context;
    }

    void saveLoginDetails(String email, String username, String password, String regu) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Username", username);
        editor.putString("Password", password);
        editor.putString("Email", email);
        editor.putString("Regu", regu);
        editor.apply();
    }

    String getUsername() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Username", "");
    }

    String getPassword() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Password", "");
    }

    String getEmail() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Password", "");
    }

    String getRegu() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Regu", "");
    }

    void clearData() {
        SharedPreferences datalogin = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editordatalogin = datalogin.edit();
        editordatalogin.clear().apply();
    }
}
