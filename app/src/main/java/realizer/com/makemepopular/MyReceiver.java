package realizer.com.makemepopular;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import realizer.com.makemepopular.service.AutoSyncService;

/**
 * Created by shree on 2/14/2017.
 */
public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
       // Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
        Intent ser = new Intent(context, AutoSyncService.class);
        ser.putExtra("FirstTime", "1");
        context.startService(ser);
    }
}