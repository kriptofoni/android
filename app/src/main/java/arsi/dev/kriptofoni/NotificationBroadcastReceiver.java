package arsi.dev.kriptofoni;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    public NotificationBroadcastReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("received");
        App.setIsRunning(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isMyServiceRunning(NotificationBackgroundService.class, context)) {
                System.out.println("running service");
                context.stopService(new Intent(context, NotificationBackgroundService.class));
            }

            Intent intent1 = new Intent(context, NotificationBackgroundService.class);
            intent1.putExtra("fromBroadcast", true);
            context.startForegroundService(intent1);
        } else {
            if (!isMyServiceRunning(NotificationBackgroundService.class, context)) {
                Intent intent1 = new Intent(context, NotificationBackgroundService.class);
                intent1.putExtra("fromBroadcast", true);
                context.startService(intent1);
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
