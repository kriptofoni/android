package arsi.dev.kriptofoni;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

public class App extends Application implements LifecycleObserver {

    public static boolean isRunning = false;

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        isRunning = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "Alarms";
        String SERVICE_CHANNEL_ID = "Background Service";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Alarms", NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.setDescription("Alarm Notifications");
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

            NotificationChannel serviceChannel = new NotificationChannel(SERVICE_CHANNEL_ID, "Background Service", NotificationManager.IMPORTANCE_MIN);

            notificationChannel.setDescription("Background Service Running");
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }

    public static void setIsRunning(boolean running) {
        isRunning = running;
    }
}
