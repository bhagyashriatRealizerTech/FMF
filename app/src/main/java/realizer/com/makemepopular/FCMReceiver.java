package realizer.com.makemepopular;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import realizer.com.makemepopular.service.AutoSyncService;

/**
 * Created by shree on 2/17/2017.
 */
public class FCMReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
        Intent ser = new Intent(context, InstanceIdService.class);
        context.startService(ser);
    }
}
