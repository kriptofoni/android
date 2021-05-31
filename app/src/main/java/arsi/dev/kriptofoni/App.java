package arsi.dev.kriptofoni;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "Alarms";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,"Alarms", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Alarm Notifications");
            notificationChannel.enableVibration(true);
            notificationChannel.enableLights(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
