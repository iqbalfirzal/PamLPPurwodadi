package pendataan.parkir.kedungpane;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private final Context context;

    PrefManager(Context context) {
        this.context = context;
    }

    void saveLoginDetails(String username, String password, String regu) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Username", username);
        editor.putString("Password", password);
        editor.putString("Regu", regu);
        editor.apply();
    }

    String getRegu() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("Regu", "");
    }

    boolean isUserLogedOut() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        boolean isUsernameEmpty = sharedPreferences.getString("Username", "").isEmpty();
        boolean isPasswordEmpty = sharedPreferences.getString("Password", "").isEmpty();
        return isUsernameEmpty || isPasswordEmpty;
    }

    void clearData() {
        SharedPreferences datalogin = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editordatalogin = datalogin.edit();
        editordatalogin.clear().apply();
    }
}
