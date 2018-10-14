package jp.ac.titech.itpro.sdl.phototaker;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.util.SparseArray;
import android.widget.EditText;
import android.widget.ImageView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.provider.MediaStore;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.app.Activity;

import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

//TODO

public class MainActivity extends AppCompatActivity {

    private FaceDetector faceDetector;
    private final static int REQ_PHOTO = 1234;
    private static final int RESULT_PICK_IMAGEFILE = 1001;
    public Bitmap photoImg = null;
    public List<Integer> cx = new ArrayList<>();
    public List<Integer> cy = new ArrayList<>();
    public EditText tweett;
    public File f;
    public Uri mImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT > 15) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        faceDetector = new FaceDetector.Builder(this)
                .setTrackingEnabled(false)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        setContentView(R.layout.activity_main);
        Button photoButton = findViewById(R.id.photo_button);
        Button browseButton = findViewById(R.id.browse_button);
        final TextInputLayout twettbox = findViewById(R.id.tweettextsq);
        tweett = (EditText) findViewById(R.id.tweettext);
        final Button decorate_button = findViewById(R.id.decorate_button);

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twettbox.setVisibility(View.GONE);
                decorate_button.setText("DECORATE");

                String filename = System.currentTimeMillis() + ".jpg";

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                mImageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                Intent intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(intent, REQ_PHOTO);

                /*

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQ_PHOTO);
                PackageManager packageManager = getPackageManager();
                List activities = packageManager
                        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (activities.size() > 0) {
                    startActivityForResult(intent, REQ_PHOTO);
                }
                else {
                    Toast.makeText(MainActivity.this,
                            R.string.toast_no_activities, Toast.LENGTH_LONG).show();
                }
                */
            }
        }
        );
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decorate_button.setText("DECORATE");
                twettbox.setVisibility(View.GONE);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, RESULT_PICK_IMAGEFILE);
                //BROWSE_PHOTO
            }
        });

        decorate_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Integer> cx = new ArrayList<>();
                List<Integer> cy = new ArrayList<>();
                if (photoImg == null) return;
                //DECORATE PHOTO
                if (decorate_button.getText().equals("DECORATE")){
                    Frame frame = new Frame.Builder().setBitmap(photoImg).build();
                    SparseArray<Face> faces = faceDetector.detect(frame);
                    for (int i = 0, size = faces.size(); i < size; i++) {
                        Face face = faces.valueAt(i);
                        for (Landmark landmark : face.getLandmarks()) {
                            cx.add((int) (landmark.getPosition().x));
                            cy.add((int) (landmark.getPosition().y));
                            //what does the position mean (?)
                        }
                    }
                    //TODO
                    int ax = 0;
                    int ay = 0;
                    int maxx = -1;
                    int maxy = -1;
                    int minx = 999;
                    int miny = 999;
                    for (int i = 0; i < cx.size(); i++) {
                        ax += cx.get(i);
                        ay += cy.get(i);
                        if (maxx < cx.get(i)) {
                            maxx = cx.get(i);
                        }
                        if (maxy < cy.get(i)) {
                            maxy = cy.get(i);
                        }
                        if (minx > cx.get(i)) {
                            minx = cx.get(i);
                        }
                        if (miny > cy.get(i)) {
                            miny = cy.get(i);
                        }
                    }
                    ax = ax / cx.size();
                    ay = ay / cy.size();
                    Log.d("MainActivity", cx.toString());
                    Log.d("MainActivity", cy.toString());
                    Bitmap droid = BitmapFactory.decodeResource(getResources(), R.drawable.androkun);
                    //adjust the size
                    int width = photoImg.getWidth();
                    int height = photoImg.getHeight();
                    float tempw = maxx - minx;
                    float temph = maxy - miny;
                    droid = droid.createScaledBitmap(droid, (int) (tempw * 1.5), (int) (temph * 1.5), false);
                    Log.d("MainActivity_z", Integer.toString(maxx - minx));
                    Log.d("MainActivity_a", Integer.toString(maxy - miny));

                    Log.d("MainActivity_b", Integer.toString(width));
                    Log.d("MainActivity_c", Integer.toString(height));
                    //Backgroud size 384,384
                    Bitmap newbitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(newbitmap);
                    canvas.drawBitmap(photoImg, 0, 0, (Paint) null);
                    //adjust the position (0,0) is the center
                    // for lenna cx = [201, 232, 227, 243, 200, 161, 157]
                    //   cy = [270, 253, 282, 208, 208, 234, 202]
                    canvas.drawBitmap(droid, ax - (maxx - minx) / 2 - minx / 10, ay - (maxy - miny) / 2 - miny / 10, (Paint) null);
                    Log.d("MainActivity_d", String.valueOf(ax - (maxx - minx) / 2));
                    Log.d("MainActivity_e", String.valueOf(ay - (maxy - miny) / 2));
                    photoImg = newbitmap;
                    showPhoto();
                    decorate_button.setText("TWEET");
                }else{
                    if(twettbox.getVisibility() == View.GONE) {
                        twettbox.setVisibility(View.VISIBLE);
                    }else {
                        ConfigurationBuilder cb = new ConfigurationBuilder();
                        cb.setDebugEnabled(true)
                                .setOAuthConsumerKey("")
                                .setOAuthConsumerSecret("")
                                .setOAuthAccessToken("")
                                .setOAuthAccessTokenSecret("");
                        TwitterFactory tf = new TwitterFactory(cb.build());
                        Twitter twitter = tf.getInstance();

                        BtoF(getApplicationContext());

                        try {
                            String tweet = tweett.getText().toString();
                            if (tweet != null) {
                                StatusUpdate statusUpdate = new StatusUpdate(tweet);
                                statusUpdate.setMedia(f);
                                Status status = twitter.updateStatus(statusUpdate);
                                Log.d("tweet", "SUCCES TWEET");
                            } else {
                                StatusUpdate statusUpdate = new StatusUpdate("");
                                statusUpdate.setMedia(f);
                                Status status = twitter.updateStatus(statusUpdate);
                                Log.d("tweet", "SUCCES TWEET");
                            }
                            twettbox.setVisibility(View.GONE);
                            decorate_button.setText("DECORATE");
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        });
    }

    public  void BtoF(Context context){
        //create a file to write bitmap data
        f = new File(context.getCacheDir(), "filename");
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        photoImg.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showPhoto() {
        if (photoImg == null) return;
        ImageView photoView = findViewById(R.id.photo_view);
        photoView.setImageBitmap(photoImg);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if (resCode == RESULT_OK && reqCode == REQ_PHOTO) {
            //TODO:
            try {
                photoImg = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            showPhoto();
        } else if (reqCode == RESULT_PICK_IMAGEFILE && resCode == Activity.RESULT_OK) {
            if (data.getData() != null) {

                ParcelFileDescriptor pfDescriptor = null;
                try {
                    Uri uri = data.getData();
                    // Uriを表示
                    pfDescriptor = getContentResolver().openFileDescriptor(uri, "r");
                    if (pfDescriptor != null) {
                        FileDescriptor fileDescriptor = pfDescriptor.getFileDescriptor();
                        Bitmap bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        pfDescriptor.close();
                        photoImg = bmp;
                        showPhoto();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (pfDescriptor != null) {
                            pfDescriptor.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showPhoto();
    }
}
