package com.xlsoft.kudanar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;


import com.xlsoft.kudanar.R;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import eu.kudan.kudan.ARActivity;
import eu.kudan.kudan.ARImageNode;
import eu.kudan.kudan.ARImageTrackable;
import eu.kudan.kudan.ARImageTracker;
import eu.kudan.kudan.ARNode;
import eu.kudan.kudan.ARTexture2D;
import eu.kudan.kudan.ARTextureMaterial;

/**
 * Created by Muhammad on 24/04/2018.
 */

public class ARDisplay extends ARActivity implements Thread.UncaughtExceptionHandler {

    private ARImageTrackable trackable;
    ARImageNode imageNode;
    ARTextureMaterial textureMaterial;
    ARTexture2D texture;
    ARImageTracker trackableManager;

    Button home, share, save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arcamera);

        /*addImageTrackable();
        addImageNode();
        trackable.getWorld().getChildren().get(0).setVisible(true);*/

    }
   Boolean setsharecheck=false;
    @Override
    protected void onResume() {
        super.onResume();

        addImageTrackable();
        addImageNode();
        trackable.getWorld().getChildren().get(0).setVisible(true);

        if (trackable != null)

        {
           // hideAll();
        }


    }

    @Override
    public void setup() {
        addImageTrackable();
        addImageNode();
    }



    private void addImageTrackable() {
        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/track.png";

            ImageView img=(ImageView) findViewById(R.id.track);
            //img.setImageBitmap(BitmapFactory.decodeFile(path));
            // Initialise image trackable
            trackable = new ARImageTrackable();
            trackable.loadFromPath(path);
            // Get instance of image tracker manager
             trackableManager = ARImageTracker.getInstance();
            trackableManager.initialise();
            // Add image trackable to image tracker manager
            trackableManager.addTrackable(trackable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


private String iname;

    private void addImageNode() {

        try {
            String path = Environment.getExternalStorageDirectory().toString() + "/node.png";
            ImageView img=(ImageView) findViewById(R.id.node);
            //img.setImageBitmap(BitmapFactory.decodeFile(path));
            Log.d("Path got", path);
             texture = new ARTexture2D();
            texture.loadFromPath(path);
            texture.setTextureID(1);
            // Initialise image node
            imageNode = new ARImageNode(texture);
            // Add image node to image trackable
            trackable.getWorld().addChild(imageNode);
            // Image scale
            textureMaterial = (ARTextureMaterial) imageNode.getMaterial();
            float scale = trackable.getWidth() / textureMaterial.getTexture().getWidth();
            imageNode.scaleByUniform(scale);
            // Hide image node
            imageNode.setVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openFile(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    public void permissionsNotSelected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions Requred");
        builder.setMessage("Please enable the requested permissions in the app settings in order to use this demo app");
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                System.exit(1);
            }
        });
        AlertDialog noInternet = builder.create();
        noInternet.show();
    }

    public void permissionsRequest() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 111);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deletesaves();
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

    @Override
    public void onBackPressed() {
        //hideAll();
        trackable = null;
         imageNode=null;
         textureMaterial=null;
         texture=null;
         trackableManager=null;



        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();

    }

    private void hideAll() {
        List<ARNode> nodes = trackable.getWorld().getChildren();
        for (ARNode node : nodes) {
            node.setVisible(false);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //hideAll();

    }

    @Override
    protected void onStop() {


        trackable = null;
        imageNode=null;
        textureMaterial=null;
        texture=null;
        trackableManager=null;
        super.onStop();
    }


    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        Log.e("UnCaught Error",throwable.getMessage());

    }
}