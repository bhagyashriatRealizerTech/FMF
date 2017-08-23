package realizer.com.makemepopular.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v4.util.DebugUtils;

import realizer.com.makemepopular.service.AutoSyncService;

/**
 * Created by shree on 2/23/2017.
 */
public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        switch (action) {
            case ConnectivityManager.CONNECTIVITY_ACTION:

                if(NetworkUtils.isConnect(context)){
                    Intent ser = new Intent(context, AutoSyncService.class);
                    ser.putExtra("FirstTime", "");
                    context.startService(ser);
                }
                else
                {
                   Config.alertDialog(context,"Internet Connection","Make sure your device is connected to the internet.");
                }
                break;
        }
    }
}
