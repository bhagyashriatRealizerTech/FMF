package realizer.com.makemepopular;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import realizer.com.makemepopular.asynctask.UserProfileAsyncTaskPut;
import realizer.com.makemepopular.exceptionhandler.ExceptionHandler;
import realizer.com.makemepopular.utils.Config;
import realizer.com.makemepopular.utils.FontManager;
import realizer.com.makemepopular.utils.GetImages;
import realizer.com.makemepopular.utils.ImageStorage;
import realizer.com.makemepopular.utils.OnTaskCompleted;
import realizer.com.makemepopular.utils.Utility;
import realizer.com.makemepopular.view.ProgressWheel;

/**
 * Created by shree on 1/24/2017.
 */
public class UserProfileActivity extends AppCompatActivity implements OnTaskCompleted
{
    TextView email,dob,phone,setting_new_prof;
    String ThumbnailUrl;
    ImageView userimg;
    ImageView userimgbg;
    String imagebase64 = "";
    ProgressWheel loading;
    private Uri fileUri;
    Bitmap bitmap;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;
    String localPath="";
    String image64bit="";
    String UserID;
    MessageResultReceiver resultReceiver;
    final int CROP_PIC = 3;
    private Uri picUri;
    private static Bitmap convertedImage;
    UserProfileAsyncTaskPut thumbnailPut;
    static boolean isRespondingAPI=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this,"Login"));
        setContentView(R.layout.activity_user_profile);
        getSupportActionBar().hide();

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(UserProfileActivity.this);
        String ContactNo=sharedpreferences.getString("UserContactNo", "");
        UserID=sharedpreferences.getString("UserId","");
        ThumbnailUrl =sharedpreferences.getString("ThumbnailURL", "");
        String Gender=sharedpreferences.getString("Gender", "");
        String Fname=sharedpreferences.getString("Fname", "")+" "+sharedpreferences.getString("Lname", "");
        userimg= (ImageView) findViewById(R.id.user_img);
        userimgbg= (ImageView) findViewById(R.id.user_img_bg);
        TextView username= (TextView) findViewById(R.id.user_name);
        setting_new_prof= (TextView) findViewById(R.id.setting_new_profile);
        loading= (ProgressWheel) findViewById(R.id.loading);

        realizer.com.makemepopular.utils.Singleton obj = realizer.com.makemepopular.utils.Singleton.getInstance();
        resultReceiver = new MessageResultReceiver(null);
        obj.setResultReceiver(resultReceiver);

        username.setText(Fname.toString());
        username.setTypeface(FontManager.getTypeface(this,FontManager.FONTAWESOME2));

        email= (TextView) findViewById(R.id.user_prof_email_ico);
        dob= (TextView) findViewById(R.id.user_prof_dob_ico);
        phone= (TextView) findViewById(R.id.user_prof_phone_ico);

        email.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        dob.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));
        phone.setTypeface(FontManager.getTypeface(this, FontManager.FONTAWESOME));

        TextView email1,dob1,phone1;
        email1= (TextView) findViewById(R.id.setting_user_emailId);
        dob1= (TextView) findViewById(R.id.setting_user_dob);
        phone1= (TextView) findViewById(R.id.setting_user_phoneno);

        setting_new_prof.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // getOption();
                selectImage();
            }
        });

        userimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        email1.setText(sharedpreferences.getString("EmailId", "").toString());
        phone1.setText(ContactNo);
        String[] date=sharedpreferences.getString("DOB", "").split("T");
        String[] longdate=date[0].split("-");
        dob1.setText(longdate[2]+" "+Config.getMonth(Integer.valueOf(longdate[1]))+" "+longdate[0]);
        SetThumbnail();
        //userimg.setImageBitmap(getBitmapFromURL(ThumbnailUrl.replace("/small","")));
        //userimgbg.setImageBitmap(getBitmapFromURL(ThumbnailUrl));
    }
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
    public void SetThumbnail()
    {
        if (ThumbnailUrl.equals("")||ThumbnailUrl.equals("null")||ThumbnailUrl.equals(null))
        {

        }
        else{
            String newURL= Utility.getURLImage(ThumbnailUrl);
            if(!ImageStorage.checkifImageExists(newURL.split("/")[newURL.split("/").length - 1]))
                new GetImages(newURL,userimg,newURL.split("/")[newURL.split("/").length-1]).execute(newURL);
            else
            {
                File image = ImageStorage.getImage(newURL.split("/")[newURL.split("/").length-1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                userimg.setImageBitmap(bitmap);
            }


            String newURL2= Utility.getURLImage(ThumbnailUrl.replace("/small",""));
            if(!ImageStorage.checkifImageExists(newURL2.split("/")[newURL2.split("/").length - 1]))
                new GetImages(newURL2,userimgbg,newURL2.split("/")[newURL2.split("/").length-1]).execute(newURL2);
            else
            {
                File image = ImageStorage.getImage(newURL2.split("/")[newURL2.split("/").length-1]);
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
                Drawable drawable = new BitmapDrawable(bitmap);
                userimgbg.setBackground(drawable);
            }

        }

    }
    public void getOption() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
        galleryIntent.setType("image/*");
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
        galleryIntent.putExtra("crop", "true");
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, "Choose Action");

        Intent[] intentArray = {cameraIntent};

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
        startActivityForResult(chooser, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }
    public Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type));
    }
    private static File getOutputMediaFile(int type) {

        //External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Config.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            File sdcard = Environment.getExternalStorageDirectory() ;

            File folder = new File(sdcard.getAbsoluteFile(), Config.IMAGE_DIRECTORY_NAME);//the dot makes this directory hidden to the user
            folder.mkdir();

        }

        // Create search_layout media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");


        return mediaFile;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            String path="";
            if (requestCode == 2) {
                picUri = data.getData();
                try
                {
                    convertedImage = MediaStore.Images.Media.getBitmap(getContentResolver() , picUri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    convertedImage.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                }
                catch (Exception e)
                {
                    //handle exception
                }
                performCrop();

            }
            else if (requestCode == 1) {
                //get the Uri for the captured image
                try
                {
                    convertedImage = MediaStore.Images.Media.getBitmap(getContentResolver() , picUri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    convertedImage.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                }
                catch (Exception e)
                {
                    //handle exception
                }
                //carry out the crop operation
                performCrop();
            }
            else if (requestCode == CROP_PIC) {
                Bundle extras = data.getExtras();
                if(extras != null ) {
                    Bitmap photo = extras.getParcelable("data");
                    setPhoto(photo);
                    userimg.setImageBitmap(photo);
                    path = encodephoto(photo);
                    imagebase64 = path;
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("ProfilePicPath", path);
                    editor.commit();
                    UploadThumbnail();
                }
                else
                {
                    setPhoto(convertedImage);
                    userimg.setImageBitmap(convertedImage);
                    path = encodephoto(convertedImage);
                    imagebase64 = path;
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("ProfilePicPath", path);
                    editor.commit();
                    UploadThumbnail();
                }
            }
        } else if (resultCode == RESULT_CANCELED) {

            // user cancelled Image capture
            //Toast.makeText(getApplicationContext(),"User cancelled image capture", Toast.LENGTH_SHORT).show();
            if (requestCode != CROP_PIC) {
                Config.alertDialog(this, "Image Cancelled", "User cancelled image capture.");
            }

        } else {
            // failed to capture image
            //Toast.makeText(getApplicationContext(),"Sorry! Failed to capture image", Toast.LENGTH_SHORT).show();
            Config.alertDialog(this, "Image Error","Sorry! Failed to capture image.");
        }

    }

    //Encode image to Base64 to send to server
    private void setPhoto(Bitmap bitmapm) {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                /*Log.d(TAG, "Oops! Failed create "
                        + Config.IMAGE_DIRECTORY_NAME + " directory");*/

            }
        }
        else {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmapm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            //4
            File file = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpeg");
            try {
                file.createNewFile();
                FileOutputStream fo = new FileOutputStream(file);
                //5
                fo.write(bytes.toByteArray());
                fo.close();
                //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file)));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Intent mediaScanIntent = new Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(file);
                    mediaScanIntent.setData(contentUri);
                    this.sendBroadcast(mediaScanIntent);
                } else {
                    sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_MOUNTED,
                            Uri.parse("file://"
                                    + Environment.getExternalStorageDirectory())));
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }
    }
    private void launchUploadActivity(Intent data) {

        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userid = sharedpreferences.getString("UidName", "");
        if (data.getData() != null) {
            try {
                if (bitmap != null) {
                    //bitmap.recycle();
                }

                InputStream stream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                /*localPath = ImageStorage.saveEventToSdCard(bitmap, "userImages", UserProfileActivity.this);*/
                userimg.setImageBitmap(bitmap);
                String path = encodephoto(bitmap);
                image64bit=path;


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            bitmap = (Bitmap) data.getExtras().get("data");
            localPath = ImageStorage.saveEventToSdCard(bitmap, "userImages",UserProfileActivity.this);
            userimg.setImageBitmap(bitmap);
            String path = encodephoto(bitmap);
            image64bit=path;
        }
        UploadThumbnail();
    }
    //Encode image to Base64 to send to server
    private String encodephoto(Bitmap bitmapm) {
        String imagebase64string = "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmapm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            byte[] byteArrayImage = baos.toByteArray();
            imagebase64string = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagebase64string;
    }

    public void UploadThumbnail()
    {
        if (Config.isConnectingToInternet(UserProfileActivity.this)) {
            loading.setVisibility(View.VISIBLE);
            userimg.setEnabled(false);
            thumbnailPut = new UserProfileAsyncTaskPut(UserID, imagebase64, UserProfileActivity.this, UserProfileActivity.this);
            thumbnailPut.execute();
            isRespondingAPI=false;
            new CountDownTimer( 15000, 1000) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    loading.setVisibility(View.GONE);
                    if (!isRespondingAPI)
                    {
                        if (thumbnailPut != null && thumbnailPut.getStatus() != AsyncTask.Status.FINISHED) {
                            thumbnailPut.cancel(true);
                            Config.alertDialog(UserProfileActivity.this, "Error", "Server is not responding.Please try again after sometime.");
                        }
                    }
                }
            }.start();
        }
        else {
            Config.alertDialog(UserProfileActivity.this,"Internet Connection","Make sure your device is connected to the internet.");
        }
    }

    @Override
    public void onTaskCompleted(String s) {
        isRespondingAPI=true;
        SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loading.setVisibility(View.GONE);
        userimg.setEnabled(true);
        String[] onTask=s.split("@@@");
        if (onTask[1].equals("ProfilePic"))
        {
            JSONObject rootObj = null;
            Log.d("String", onTask[0]);
            try {

                rootObj = new JSONObject(onTask[0]);
                String thumbnailUrl = rootObj.getString("thumbnailUrl");
                String userid=rootObj.getString("userId");
                if (!userid.equalsIgnoreCase("00000000-0000-0000-0000-000000000000")) {
                    SharedPreferences.Editor edit = sharedpreferences.edit();
                    edit.putString("ThumbnailURL", thumbnailUrl);
                    edit.commit();
                    ThumbnailUrl = thumbnailUrl;
                }

            } catch (JSONException e) {
                e.printStackTrace();

                /*Log.e("JSON", e.toString());
                Log.e("Login.JLocalizedMessage", e.getLocalizedMessage());
                Log.e("Login(JStackTrace)", e.getStackTrace().toString());
                Log.e("Login(JCause)", e.getCause().toString());
                Log.wtf("Login(JMsg)", e.getMessage());*/
            }
            SetThumbnail();
        }
    }
    class UpdateUI implements Runnable {
        String update;

        public UpdateUI(String update) {

            this.update = update;
        }

        public void run() {

            SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(UserProfileActivity.this);
            if(update.equals("Emergency")) {

                String notType=sharedpreferences.getString("Type", "");
                if (notType.equalsIgnoreCase("FriendRequest"))
                {
                    String reqstName=sharedpreferences.getString("RequsetByName", "");
                    String thumbnail=sharedpreferences.getString("ThumbnailUrl", "");
                    Config.showacceptrejectFriendRequest(reqstName,thumbnail,UserProfileActivity.this);
                    //Config.showacceptrejectFriendRequest(reqstName,UserProfileActivity.this);
                }
                else if (notType.equalsIgnoreCase("Emergency"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String trobler=sharedpreferences.getString("TroublerName", "");
                    String troblerid=sharedpreferences.getString("TroublerUserId", "");
                    Config.showEmergencyAcceptReject(msg,trobler,troblerid,UserProfileActivity.this);
                }
                else if (notType.equalsIgnoreCase("EmergencyRecipt"))
                {
                    String msg=sharedpreferences.getString("Message", "");
                    String helpername=sharedpreferences.getString("HelperUserName", "");
                    String isResch=sharedpreferences.getString("isReaching", "");
                    if (isResch.equalsIgnoreCase("true")){}
                    //showEmergencyAckAlert(newMsg, helpername);
                    Config.showEmergencyAckAlert(msg,helpername,UserProfileActivity.this);
                }

            }

            else if(update.equals("RefreshThreadList")) {

            }
        }
    }

    class MessageResultReceiver extends ResultReceiver
    {
        public MessageResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if(resultCode == 300){
                UserProfileActivity.this.runOnUiThread(new UpdateUI("Emergency"));
            }
            if(resultCode == 200){
                UserProfileActivity.this.runOnUiThread(new UpdateUI("RefreshThreadList"));
            }

        }
    }

    private void selectImage() {

        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo"))
                {
                    try {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/picture.jpg";
                        File imageFile = new File(imageFilePath);
                        picUri = Uri.fromFile(imageFile); // convert path to Uri
                        takePictureIntent.putExtra( MediaStore.EXTRA_OUTPUT,  picUri );
                        startActivityForResult(takePictureIntent, 1);
                    } catch(ActivityNotFoundException anfe){
                        //display an error message
                        String errorMessage = "Whoops - your device doesn't support capturing images!";
                       // Toast.makeText(UserProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        Config.alertDialog(UserProfileActivity.this, "Error",errorMessage+".");
                    }
                }
                else if (options[item].equals("Choose from Gallery"))
                {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 2);//one can be replaced with any action code
                }
                else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        String mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    /**
     * this function does the crop operation.
     */
    private void performCrop(){

       /* Intent intent = new Intent("com.android.camera.action.CROP");
        //intent.setClassName("com.android.camera", "com.android.camera.CropImage");
        intent.setData(picUri);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 96);
        intent.putExtra("outputY", 96);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_PIC);*/

        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 0);
            cropIntent.putExtra("aspectY", 0);
            //indicate output X and Y
            cropIntent.putExtra("outputX",512);
            cropIntent.putExtra("outputY", 512);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, CROP_PIC);
        }
        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!.Setting original image to profile.";
//            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
//            toast.show();
            Config.alertDialog(UserProfileActivity.this, "Error",errorMessage+".");
            String path="";
            setPhoto(convertedImage);
            userimg.setImageBitmap(convertedImage);
            path = encodephoto(convertedImage);
            imagebase64 = path;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("ProfilePicPath", path);
            editor.commit();
            UploadThumbnail();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            String path="";
            setPhoto(convertedImage);
            userimg.setImageBitmap(convertedImage);
            path = encodephoto(convertedImage);
            imagebase64 = path;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("ProfilePicPath", path);
            editor.commit();
            UploadThumbnail();
        }
    }
}
