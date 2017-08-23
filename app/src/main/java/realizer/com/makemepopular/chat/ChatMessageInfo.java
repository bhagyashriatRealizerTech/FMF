package realizer.com.makemepopular.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import realizer.com.makemepopular.R;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;

/**
 * Created by shree on 3/14/2017.
 */
public class ChatMessageInfo extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,""));
        setContentView(R.layout.message_info_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Message Info");

        TextView sname=(TextView) findViewById(R.id.txtsenderName);
        TextView message=(TextView) findViewById(R.id.txtMessage);
        TextView msgtime=(TextView) findViewById(R.id.txttime);

        Bundle b = getIntent().getExtras();

        sname.setText(b.getString("SName"));
        message.setText(b.getString("MEssage"));
        msgtime.setText(b.getString("MSgtime"));
        Log.d("Invoked","onCreate");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Invoked","onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Invoked","onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Invoked","onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Invoked","onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Invoked","onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("Invoked","onRestart");
    }
}
