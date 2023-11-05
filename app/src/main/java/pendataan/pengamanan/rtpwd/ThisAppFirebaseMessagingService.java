package pendataan.pengamanan.rtpwd;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Objects;

import static android.graphics.Color.rgb;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class ThisAppFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Intent intent;

        String title = Objects.requireNonNull(remoteMessage.getNotification()).getTitle();
        String body = remoteMessage.getNotification().getBody();

        Map<String, String> extraData = remoteMessage.getData();
        String senderid = extraData.get("senderid");
        String messagetype = extraData.get("messagetype");
        String sendername = extraData.get("sendername");
        String senderregu = extraData.get("senderregu");
        String senderphoto = extraData.get("senderphoto");
        String senderlocationlat = extraData.get("senderlocation_lat");
        String senderlocationlongi = extraData.get("senderlocation_longi");
        String sendTo = extraData.get("sendto");
        String iduser = new PrefManager(this).getNip();

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "PLPSmgApp")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSmallIcon(R.drawable.ic_warning)
                        .setColor(rgb(255, 0, 0));
        assert senderid != null; assert messagetype != null;
        if(messagetype.equals("emergency")&&!senderid.equals(iduser)) {
            intent = new Intent(this, ShowEmergencyCall.class);
            intent.putExtra("senderid", senderid);
            intent.putExtra("sendername", sendername);
            intent.putExtra("senderregu", senderregu);
            intent.putExtra("senderphoto", senderphoto);
            intent.putExtra("senderlocation_lat", senderlocationlat);
            intent.putExtra("senderlocation_longi", senderlocationlongi);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setFullScreenIntent(pendingIntent,true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            int id =  (int) System.currentTimeMillis();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel("PLPSmgApp","emergencycallnotif",NotificationManager.IMPORTANCE_MAX);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(id,notificationBuilder.build());
        }else if(messagetype.equals("balasanlapsus")&&Objects.requireNonNull(sendTo).equals(iduser)){
            intent = new Intent(this, MainActivity3.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setFullScreenIntent(pendingIntent,true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            int id =  (int) System.currentTimeMillis();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel("PLPSmgApp","emergencycallnotif",NotificationManager.IMPORTANCE_MAX);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(id,notificationBuilder.build());
        }
    }
}
