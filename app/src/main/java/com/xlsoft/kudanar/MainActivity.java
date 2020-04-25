package com.xlsoft.kudanar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import eu.kudan.kudan.ARAPIKey;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.translate.AmazonTranslateAsyncClient;
import com.amazonaws.services.translate.model.TranslateTextRequest;
import com.amazonaws.services.translate.model.TranslateTextResult;
import com.xlsoft.kudanar.Database.DbConstants;
import com.xlsoft.kudanar.camera.CameraFragment;
import numl.fyp.ashs.ltu_new.utils.ImageOps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.xlsoft.kudanar.Database.DbOperations;

import com.xlsoft.kudanar.TextDetect.TextDetect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements Thread.UncaughtExceptionHandler {

    String selectedLangs="";
    FrameLayout fragContainer;
    ImageView imageView;
    ImageOps imageOps;
    TextDetect textDetect;
    TextView textView,textView_urdu;
    CameraFragment cameraFragment;
    String[] alphabets;
    DbOperations dbOperations;
    ProgressBar progressbar;
    RadioButton rb_online,rb_offline;
    RadioButton rb_solid,rb_semi,rb_traansparent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread.setDefaultUncaughtExceptionHandler(this);

        ARAPIKey key = ARAPIKey.getInstance();
        key.setAPIKey(getResources().getString(R.string.kudanApikey));

        permissionsRequest();

        progressbar=findViewById(R.id.progressbar);
        dbOperations=new DbOperations(this);
        alphabets = DbConstants.ALPHABETS;
        textView=findViewById(R.id.textview);
        textView_urdu=findViewById(R.id.textview_urdu);
        imageOps=new ImageOps();
        textDetect = new TextDetect(this);
        selectedLangs=getIntent().getStringExtra("extra");
        fragContainer=findViewById(R.id.fragContainer);
        cameraFragment=new CameraFragment(this);
        imageView=findViewById(R.id.img);


        findViewById(R.id.picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(rb_online.isChecked()){
                    if(!haveNetworkConnection()){
                        Toast.makeText(MainActivity.this,"Internet not Connected",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


                if(cameraFragment.isReady()){
                    cameraFragment.takePicture();
                }
            }
        });


        rb_online=findViewById(R.id.rb_online);
        rb_offline=findViewById(R.id.rb_offline);
        rb_solid=findViewById(R.id.rb_solid);
        rb_semi=findViewById(R.id.rb_semi);
        rb_traansparent=findViewById(R.id.rb_transparent);

        rb_offline.setChecked(true);
        rb_solid.setChecked(true);




        getSupportFragmentManager().beginTransaction().add(R.id.fragContainer,cameraFragment).commit();

        //Toast.makeText(this,dbOperations.getRecord("a","apple"),Toast.LENGTH_SHORT).show();



       // getOnlineToken();

    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    public void permissionsRequest() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 111);

        }
    }

    public void refreshCameraFragment(){

        getSupportFragmentManager().beginTransaction().remove(cameraFragment).commit();
        cameraFragment=new CameraFragment(this);
        getSupportFragmentManager().beginTransaction().add(R.id.fragContainer,cameraFragment).commit();

    }

    Bitmap referenceImage=null;
    Bitmap translatedImage=null;

    /*public boolean IS_ONLINE=false;
    public boolean IS_SOLID=true,IS_SEMI=false;*/

    public void OnImageCaptured(final byte[] bytes, final Rect areaRect){


        if(rb_online.isChecked()){

            onlineTask(bytes,areaRect);

        }
        else{

            offlineTask(bytes,areaRect);
        }




    }

    Bitmap Originalimage;
    private void onlineTask(final byte[] bytes, final Rect areaRect) {



        new AsyncTask<Void,Void,Void>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressbar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                BitmapFactory.Options options=new BitmapFactory.Options();

                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;

                Originalimage=imageOps.getFullScreenImage(
                        BitmapFactory.decodeByteArray(bytes,0,bytes.length,options)
                        ,width,height
                );

                referenceImage=imageOps.getCropedImage(Originalimage, areaRect.left,areaRect.top,areaRect.width(),areaRect.height());

                gottext = textDetect.recognize(referenceImage).trim();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);


                if(!gottext.equals("")){
                    isTextFound=true;
                    textView.setText(gottext);

                    onlineTranslation(gottext, new AsyncHandler<TranslateTextRequest, TranslateTextResult>() {
                        @Override
                        public void onError(final Exception e) {
                            Log.e("amazon", "Error occurred in translating the text: " + e.getLocalizedMessage());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Toast.makeText(MainActivity.this,"Error: Unstable Internet or wrong Aoi keys",Toast.LENGTH_LONG).show();
                                    //Toast.makeText(MainActivity.this,"Error: "+e.getMessage(),Toast.LENGTH_LONG).show();
                                    progressbar.setVisibility(View.GONE);
                                }
                            });

                        }

                        @Override
                        public void onSuccess(TranslateTextRequest request, TranslateTextResult translateTextResult) {

                            try{

                                urduText= translateTextResult.getTranslatedText().replace("","").trim();

                                Log.d("urduText",urduText);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView_urdu.setText(urduText);
                                    }
                                });

                                translatedImage=getUrduImage(urduText,Originalimage);

                                savetrackable(referenceImage);
                                saveNode(translatedImage);

                                Intent i=new Intent(MainActivity.this,ARDisplay.class);
                                startActivity(i);
                                finish();
                            }
                            catch (Exception e){

                                e.printStackTrace();
                            }




                            Log.d("amazon", "Translated Text: " + translateTextResult.getTranslatedText());
                        }
                    });


                }
                else {
                    textView.setText("");
                    textView_urdu.setText("");
                    isTextFound=false;
                    Toast.makeText(MainActivity.this,"No Text Found",Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                }



            }

        }.execute();





    }

    private void offlineTask(final byte[] bytes, final Rect areaRect) {

        new AsyncTask<Void,Void,Void>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressbar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {

                offlineBackGroundTask( bytes,  areaRect);


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressbar.setVisibility(View.GONE);
                if(isTextFound){
                    //Toast.makeText(getApplicationContext(),gottext,Toast.LENGTH_SHORT).show();
                    Intent i=new Intent(MainActivity.this,ARDisplay.class);
                    startActivity(i);
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(),"No Text found Please retry!",Toast.LENGTH_SHORT).show();
                }

                //refreshCameraFragment();
            }
        }.execute();


    }

    String gottext;
    String urduText;
    boolean isTextFound=false;
    private void offlineBackGroundTask(byte[] bytes, Rect areaRect) {
        BitmapFactory.Options options=new BitmapFactory.Options();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        Bitmap Originalimage=imageOps.getFullScreenImage(
                BitmapFactory.decodeByteArray(bytes,0,bytes.length,options)
                ,width,height
        );

        referenceImage=imageOps.getCropedImage(Originalimage, areaRect.left,areaRect.top,areaRect.width(),areaRect.height());

         gottext = textDetect.recognize(referenceImage).trim();
        if(gottext.equals("")){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText("");
                    textView_urdu.setText("");


                }
            });
            isTextFound=false;

            return;
        }
        else {
            isTextFound=true;
        }



        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(gottext);

            }
        });


        try {

             urduText= offlineTranslation(gottext).trim();

            Log.d("urduText",urduText);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView_urdu.setText(urduText);
                }
            });

            translatedImage=getUrduImage(urduText,Originalimage);

            savetrackable(referenceImage);
            saveNode(translatedImage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getUrduImage(String urduText,Bitmap referenceImage) {

        String[] textarray=urduText.split("\n");

        int width=(int)referenceImage.getWidth();
        int height=(int)referenceImage.getHeight();


        Paint paint=new Paint();
        Paint strokepaint=new Paint();

        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);

        strokepaint.setAntiAlias(true);
        strokepaint.setColor(Color.WHITE);
        strokepaint.setStyle(Paint.Style.STROKE);
        strokepaint.setStrokeWidth(2f);

        strokepaint.setTextAlign(Paint.Align.CENTER);


        float textSize=getTextSize(urduText,paint,width);
        width=((int)(width+(textSize*2)));
        paint.setTextSize(textSize);
        strokepaint.setTextSize(textSize);
        paint.setShadowLayer(5,0,0,Color.WHITE);

        Rect r=new Rect();
        paint.getTextBounds("A",0,1,r);
        int textheight=r.height();

        height=(int)((textheight*2)+(textheight*textarray.length)+(textarray.length*(textSize*0.75)));

        Bitmap bitmap=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(bitmap);

        if(rb_semi.isChecked()){
            canvas.drawColor(getResources().getColor(R.color.semiTransparent));
        }
        else if(rb_solid.isChecked()){

          int color=getDominantColor(referenceImage);
          canvas.drawColor(color);

          if(ColorUtils.calculateLuminance(color)>0.5){
              paint.setColor(Color.BLACK);
          }
          else{
              paint.setColor(Color.WHITE);
          }

        }


        int th=textheight;
        for(int i=0;i<textarray.length;i++){

            th=th+(int)(r.height()+(r.height()*0.75));
           // bitmap.setHeight(th+(int)textSize);
            //canvas.drawText(textarray[i],width/2,th,strokepaint);
            canvas.drawText(textarray[i],width/2,th,paint);

        }



        //canvas.drawText(urduText,width/2,height/2,paint);

        return bitmap;
    }


    private static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    private float getTextSize(String urduText,Paint m_textPaint,float width) {
        //float width = 100; // define a width which should be achieved

        String[] textarray=urduText.split("\n");
        int index=0;
        int sizetemp=0;
        for(int i=0;i<textarray.length;i++){

            if(textarray[i].toCharArray().length>sizetemp){
                sizetemp=textarray[i].toCharArray().length;
                index=i;
                Log.d("urdutext","index: "+index);
            }

        }

        Log.d("urdutext","final index: "+index);

        m_textPaint.setTextSize( 100 ); // set a text size surely big enough
        Rect r = new Rect();
        m_textPaint.getTextBounds( textarray[index], 0, textarray[index].length(), r ); // measure the text with a random size
        float fac = width / r.width(); // compute the factor, which will scale the text to our target width

        return m_textPaint.getTextSize() * fac;

    }

    private String offlineTranslation(String gottext) {

        String[] words=gottext.split(" ");

        String finalurduText="";

        for(int i=0;i<words.length;i++){

            if(!words[i].isEmpty() &&isAphabet(words[i].charAt(0))){
                String urdu=dbOperations.getRecord(words[i].substring(0,1).toUpperCase(),words[i].toLowerCase());

                if(urdu.equals("")){
                    urdu=getUrduConcatinated(words[i].toLowerCase());

                }

                finalurduText=finalurduText+" "+urdu;
            }
            else {
                finalurduText=finalurduText+" "+words[i];
            }



        }

        return finalurduText;

    }

    private boolean isAphabet(char ch) {

        if( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')){
            return true;
        }

        return false;
    }

    private String getUrduConcatinated(String word) {

        String finalmsg="";
        char[] ch=word.toCharArray();

        for(int i=0;i<ch.length;i++){
            switch ( ch[i]){
                case 'a':
                    finalmsg=finalmsg+"ا";
                break;
                case 'b':
                    finalmsg=finalmsg+"ب";
                    break;
                case 'c':
                    finalmsg=finalmsg+"ک";
                    break;
                case 'd':
                    finalmsg=finalmsg+"ڈ";
                    break;
                case 'e':
                    finalmsg=finalmsg+"ا";
                    break;
                case 'f':
                    finalmsg=finalmsg+"ف";
                    break;
                case 'g':
                    finalmsg=finalmsg+"ج";
                    break;
                case 'h':
                    finalmsg=finalmsg+"ہ";
                    break;
                case 'i':
                    finalmsg=finalmsg+"ا";
                    break;
                case 'j':
                    finalmsg=finalmsg+"ج";
                    break;
                case 'k':
                    finalmsg=finalmsg+"ک";
                    break;
                case 'l':
                    finalmsg=finalmsg+"ل";
                    break;
                case 'm':
                    finalmsg=finalmsg+"م";
                    break;
                case 'n':
                    finalmsg=finalmsg+"ن";
                    break;
                case 'o':
                    finalmsg=finalmsg+"ا";
                    break;
                case 'p':
                    finalmsg=finalmsg+"پ";
                    break;
                case 'q':
                    finalmsg=finalmsg+"ق";
                    break;
                case 'r':
                    finalmsg=finalmsg+"ر";
                    break;
                case 's':
                    finalmsg=finalmsg+"س";
                    break;
                case 't':
                    finalmsg=finalmsg+"ٹ";
                    break;
                case 'u':
                    finalmsg=finalmsg+"ی";
                    break;
                case 'v':
                    finalmsg=finalmsg+"و";
                    break;
                case 'w':
                    finalmsg=finalmsg+"و";
                    break;
                case 'x':
                    finalmsg=finalmsg+"ذ";
                    break;
                case 'y':
                    finalmsg=finalmsg+"ے";
                    break;
                case 'z':
                    finalmsg=finalmsg+"ز";
                    break;

            }
        }

        return finalmsg;
    }

/*

Translation using Bing Api



    String token;
    private void getOnlineToken(){

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (true) {
                        Tokenget();
                        Thread.sleep(540000);
                    }
                } catch (InterruptedException e) {
                    // tvtime.setText("Main Thread Error: "+e.getMessage());
                }
            }
        };
        t.start();

    }

    public void Tokenget() {
        MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");

        OkHttpClient tokenclient = new OkHttpClient();

        //making request in proper format
        Request newrequest = new Request.Builder()
                .url("https://api.cognitive.microsoft.com/sts/v1.0/issueToken")
                .post(RequestBody.create(mediaType, subskey))
                .addHeader("Ocp-Apim-Subscription-Key", subskey)
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "b4b76665-c7bb-433e-a356-a87822ec1fca")
                .build();
        try {
            Response response = tokenclient.newCall(newrequest).execute();
            token = response.body().string();
            response.close();
            Log.d("New Token: ", token);


        } catch (Exception ex) {
            //  tvtime.setText("Token Renrew Error: "+ex.getMessage());
            Log.d("log", ex.getMessage());
        }
    }



    String subskey = "f568d80272f246b888b40d1b8bb9bde0";
    private String onlineTranslation(final String text) throws Exception {

        String message="";

        if ((token != null)) {

            //Online translation started
            OkHttpClient client = new OkHttpClient();
            String head = "https://api.microsofttranslator.com/V2/Http.svc/Translate?to=ur&text=";
            String body = text;
            String tail = "&Ocp-Apim-Subscription-Key=" + subskey;

            Request request = new Request.Builder()
                    .url(head + body + tail)
                    .get()
                    .addHeader("Authorization", "Bearer " + token).build();

            try {

                Response response = client.newCall(request).execute();
                message = response.body().string().replace("<string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/\">", "").replace("</string>", "");


                Log.d("log", message);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            Toast.makeText(getApplicationContext(), "ٹوکن نہیں ملا ابھی تک ۔۔۔", Toast.LENGTH_SHORT).show();

        }


        return message;


    }*/


    private void onlineTranslation(String text,AsyncHandler<TranslateTextRequest, TranslateTextResult> handler){

        AWSCredentials awsCredentials = new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return getResources().getString(R.string.amazon_accesskey);
            }

            @Override
            public String getAWSSecretKey() {
                return getResources().getString(R.string.amazon_secretkey);
            }
        };

        AmazonTranslateAsyncClient translateAsyncClient = new AmazonTranslateAsyncClient(awsCredentials);
        TranslateTextRequest translateTextRequest = new TranslateTextRequest()
                .withText(text)
                .withSourceLanguageCode("en")
                .withTargetLanguageCode("ur");
        translateAsyncClient.translateTextAsync(translateTextRequest,handler);

    }

//////////////////////////////////////////////////////////////

    public void saveNode(Bitmap bm){

        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOutputStream = null;
        File dir = new File(path );
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path, "node.png");

        try {
            fOutputStream = new FileOutputStream(file);

            bm.compress(Bitmap.CompressFormat.PNG, 100, fOutputStream);

            fOutputStream.flush();
            fOutputStream.close();

            //  MediaStore.Images.Media.insertImage(thigetContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
            // Toast.makeText(context.getApplicationContext(), "Saved Successfully", Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "FileNotFoundException", Toast.LENGTH_SHORT).show();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "FileNotFoundException", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void deletesaves(){

        String path = Environment.getExternalStorageDirectory().toString();


        File file = new File(path, "node.png");
        File file1 = new File(path , "track.png");
        if(file.exists()) {

            file.delete();
            // Toast.makeText(context.getApplicationContext(), "Deleted 1 Successfully", Toast.LENGTH_SHORT).show();
        }
        if(file1.exists()) {

            file1.delete();
            // Toast.makeText(context.getApplicationContext(), "Deleted 2 Successfully", Toast.LENGTH_SHORT).show();
        }

    }

    public void savetrackable(Bitmap bm){

        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOutputStream = null;
        File dir = new File(path );
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(path , "track.png");

        try {
            fOutputStream = new FileOutputStream(file);

            bm.compress(Bitmap.CompressFormat.PNG, 100, fOutputStream);

            fOutputStream.flush();
            fOutputStream.close();

            //  MediaStore.Images.Media.insertImage(thigetContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
            //  Toast.makeText(context.getApplicationContext(), "Saved Successfully", Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "AR Trackable Save Failed", Toast.LENGTH_SHORT).show();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "AR Tranckable Save Failed", Toast.LENGTH_SHORT).show();
            return;
        }



    }

    ///////////////////////////////////



    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {

        Log.e("Uncaught Exception",throwable.getMessage());
        throwable.printStackTrace();
    }
}
