package realizer.com.makemepopular.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import realizer.com.makemepopular.Singleton;

/**
 * Created by Bhagyashri on 2/17/2016.
 */
public class ChatNetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        Singleton obj = Singleton.getInstance();

        if (wifi.getState().equals(NetworkInfo.State.CONNECTED) || mobile.getState().equals(NetworkInfo.State.CONNECTED)) {

            Intent sendmessageservice = new Intent(context, SendMessageService.class);
            obj.setMessageService(sendmessageservice);
            context.startService(sendmessageservice);

        } else {
            if (Singleton.getMessageService() != null)
                context.stopService(Singleton.getMessageService());
            Log.d("Network", "Flag No 2");
        }
    }
}