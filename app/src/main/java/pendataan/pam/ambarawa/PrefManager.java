package pendataan.pam.ambarawa;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private final Context context;

    PrefManager(Context context) {
        this.context = context;
    }

    void saveLoginDetails(String nip, String nama, String pin, String regu, String foto) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nip", nip);
        editor.putString("nama", nama);
        editor.putString("pin", pin);
        editor.putString("regu", regu);
        editor.putString("foto", foto);
        editor.apply();
    }

    void updateLoginDetails(String nama, String pin, String regu) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nama", nama);
        editor.putString("pin", pin);
        editor.putString("regu", regu);
        editor.apply();
    }

    void updateFoto(String foto) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("foto", foto);
        editor.apply();
    }

    String getFoto() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("foto", "");
    }

    String getRegu() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("regu", "");
    }

    String getPin() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("pin", "");
    }

    String getNama() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("nama", "");
    }

    String getNip() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("nip", "");
    }

    boolean isUserLogedOut() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        boolean isEmailEmpty = sharedPreferences.getString("nip", "").isEmpty();
        boolean isPasswordEmpty = sharedPreferences.getString("pin", "").isEmpty();
        return isEmailEmpty || isPasswordEmpty;
    }

    void saveInstruksiPim(String isi) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("InstruksiPim", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("isi", isi);
        editor.apply();
    }

    String getInstruksiPim() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("InstruksiPim", Context.MODE_PRIVATE);
        return sharedPreferences.getString("isi", "[ KALAPAS ] -\n\n[ KPLP ] -");
    }

    void clearData() {
        SharedPreferences datalogin = context.getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editordatalogin = datalogin.edit();
        editordatalogin.clear().apply();
    }
}
