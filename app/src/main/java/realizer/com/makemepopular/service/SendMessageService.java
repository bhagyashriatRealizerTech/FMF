package realizer.com.makemepopular.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import realizer.com.makemepopular.Singleton;
import realizer.com.makemepopular.backend.DatabaseQueries;
import realizer.com.makemepopular.chat.asynctask.SendMessgeAsyncTaskPost;
import realizer.com.makemepopular.chat.model.ChatMessageViewListModel;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.OnTaskCompleted;

/**
 * Created by shree on 3/9/2017.
 */
public class SendMessageService extends Service implements OnTaskCompleted {

    DatabaseQueries qr;
    Timer timer;
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        qr=new DatabaseQueries(SendMessageService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        int sec=10000;
        int total_min=(60000 * 30);//AUTOSYNC call after 30 mins
        timer = new Timer();

        timer.scheduleAtFixedRate(new SendMessageDataTask(),sec,total_min);
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskCompleted(String s)
    {
    }

    class SendMessageDataTask extends TimerTask
    {
        @Override
        public void run() {
            if(Config.isConnectingToInternet(SendMessageService.this)) {
                Log.d("AutoService", "Start");
                SendSyncupMessages();
            }
        }
    }

    public void SendSyncupMessages()
    {
        ArrayList<ChatMessageViewListModel> messageList=new ArrayList<>();
        messageList=qr.getSyncupFalseThreadMessage();
        if (messageList.size()>0)
        {
            for (int i=0;i<messageList.size();i++)
            {
               if (Config.isConnectingToInternet(SendMessageService.this))
               {
                   ServiceSendMessgeAsyncTaskPost sendmessage=new ServiceSendMessgeAsyncTaskPost(SendMessageService.this,messageList.get(i),SendMessageService.this);
                   sendmessage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
               }
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Singleton.setMessageService(null);
        timer.cancel();
    }
}
