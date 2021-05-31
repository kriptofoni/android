package arsi.dev.kriptofoni;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

public class NotificationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("catched");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Intent intent1 = new Intent(context, NotificationBackgroundService.class);
                    intent1.putExtra("fromBroadcast", true);
                    context.startForegroundService(intent1);
                }
            };
            handler.postDelayed(runnable, 1000 * 60);
        } else {
            context.startService(new Intent(context, NotificationBackgroundService.class));
        }
    }
}
