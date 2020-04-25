package com.xlsoft.kudanar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.xlsoft.kudanar.Database.DbConstants;
import com.xlsoft.kudanar.Database.DbHelper;
import com.xlsoft.kudanar.Database.DbOperations;

import com.xlsoft.kudanar.R;
import com.comix.overwatch.HiveProgressView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

public class SplashScreen extends AppCompatActivity {


    DbOperations dbOperations;
    String[] alphabets;
    TextView txt_loading,txt_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        dbOperations = new DbOperations(this);
        alphabets = DbConstants.ALPHABETS;

        txt_loading=findViewById(R.id.txt_loading);
        txt_progress=findViewById(R.id.txt_progress);

        HiveProgressView progressView = (HiveProgressView) findViewById(R.id.hive_progress);
        progressView.setRainbow(false);
        progressView.setColor(getResources().getColor(R.color.progresslight  ));


        if (IsInitialSetupRequired()) {
            txt_loading.setText("First Time Setup!\nPlease wait while setting up offline dictionary");
            InitialSetup();
        } else {
            showAnimationAndMoveForward();
        }


    }

    private void showAnimationAndMoveForward() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        };
        t.start();
    }

    private void InitialSetup() {

        new AsyncTask<Void, Void, Void>() {


            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    insertRecords();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                Intent i=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
                finish();


            }
        }.execute();


    }




    int progress=0;
    private void insertRecords() throws JSONException {

        DbHelper dbHelper=new DbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.setLockingEnabled(false);

        for (int i = 0; i < alphabets.length; i++) {

            JSONObject jsonObject = new JSONObject(getJsonString(alphabets[i] + "trans"));
            // Log.d("data",jsonObject.toString());

            LinkedHashMap<String, String> alphabetDct = new LinkedHashMap<>();

            JSONArray jsonArray = jsonObject.getJSONArray("translations");

            for (int a = 0; a < jsonArray.length(); a++) {

                final JSONObject object = jsonArray.getJSONObject(a);
                Log.d("data", object.toString());
                final int position = i;

                try{

                    ContentValues values = new ContentValues();

                    values.put(DbConstants.ENGLISH, object.getString("eng"));
                    values.put(DbConstants.URDU, object.getString("urdu"));



                     db.insert(alphabets[position].toUpperCase() + DbConstants.TABLE, null, values);

                     progress=progress+1;
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             String txt="Completed "+(int)( progress*100/70000 ) +"%";
                             txt_progress.setText(txt);

                         }
                     });

                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }


        }


        db.setLockingEnabled(true);
        db.close();

    }

    public String getJsonString(String filename) {
        String json = null;
        try {
            InputStream is = getAssets().open("jsons/" + filename.toLowerCase() + ".json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


    private boolean IsInitialSetupRequired() {

        if (dbOperations.getRecord("Z", "zymurgy").equals("زیمرگی")) {
            Log.d("data", "all data in db");
            return false;
        }

        return true;
    }
}
