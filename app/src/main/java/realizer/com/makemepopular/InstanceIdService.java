package realizer.com.makemepopular;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by shree on 2/17/2017.
 */
public class InstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "InstanceIdService";

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("NewFcmIdGenerated", token);
        edit.commit();
        Log.e(TAG, "Got token: " + token);
    }
}