package realizer.com.makemepopular.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import realizer.com.makemepopular.R;


/**
 * Created by Bhagyashri on 9/12/2016.
 */
public class GetImages extends AsyncTask<Object, Object, Object> {
    private String requestUrl, imagename_;
    private ImageView view;
    private Bitmap bitmap ;
    private FileOutputStream fos;
    public GetImages(String requestUrl, ImageView view, String _imagename_) {
        this.requestUrl = requestUrl;
        this.view = view;
        this.imagename_ = _imagename_ ;
    }

    @Override
    protected Object doInBackground(Object... objects) {
        try {
            URL url = new URL(requestUrl);
            URLConnection conn = url.openConnection();
            bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            /*URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);*/
        } catch (Exception ex) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {

        if(bitmap != null) {
            if (!ImageStorage.checkifImageExists(imagename_)) {
                view.setImageBitmap(bitmap);
                ImageStorage.saveToSdCard(bitmap, imagename_);
            }
        }
        else
        {
            view.setImageResource(R.drawable.user_icon);
        }
    }
}

