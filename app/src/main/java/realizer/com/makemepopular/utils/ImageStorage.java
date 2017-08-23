package realizer.com.makemepopular.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Bhagyashri on 9/12/2016.
 */


/*Then create a class for saving and retrieving the files*/

public class ImageStorage {

    public static String saveToSdCard(Bitmap bitmap, String filename) {

        String stored = null;

        File sdcard = Environment.getExternalStorageDirectory() ;

        File folder = new File(sdcard.getAbsoluteFile(), ".UserDP");//the dot makes this directory hidden to the user
        folder.mkdir();
        File file = new File(folder.getAbsoluteFile(), filename) ;
        if (file.exists())
            return stored ;

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
            out.flush();
            out.close();
            stored = "success";
            Log.d("SavedPath1",file.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stored;
    }

    public static File getImage(String imagename) {

        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            mediaImage = new File(myDir.getPath() + "/.UserDP/"+imagename);
            Log.d("SavedPath2",mediaImage.getPath());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mediaImage;
    }

    public static boolean checkifImageExists(String imagename)
    {
        Bitmap b = null ;
        File file = ImageStorage.getImage(imagename);
        String path = file.getAbsolutePath();

        if (path != null)
            b = BitmapFactory.decodeFile(path);

        if(b == null ||  b.equals(""))
        {
            return false ;
        }
        return true ;
    }
    public static String saveEventToSdCard(Bitmap bitmap,String eventName,Context context) {

        String stored = null;
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+ File.separator+Config.IMAGE_DIRECTORY_NAME + File.separator+eventName);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {

            }
            else
            {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                //4
                File file = new File(mediaStorageDir.getPath() + File.separator
                        + "IMG_" + timeStamp + ".jpeg");

                try {
                    file.createNewFile();
                    FileOutputStream fo = new FileOutputStream(file);
                    //5
                    fo.write(bytes.toByteArray());
                    fo.close();
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                stored = file.getAbsolutePath();
            }
        } else {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS",
                    Locale.getDefault()).format(new Date());

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            //4
            File file = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpeg");

            try {
                file.createNewFile();
                FileOutputStream fo = new FileOutputStream(file);
                //5
                fo.write(bytes.toByteArray());
                fo.close();
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            stored = file.getAbsolutePath();
        }
        return stored;
    }

}
