package com.xlsoft.kudanar.TextDetect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Environment;

import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;

/**
 * Created by Muhammad on 20/04/2018.
 */

public class preAR {
    private Boolean BackCheck;
    Context context;
    public preAR(Context ocontext){
        context=ocontext;
    }
    public preAR(){

    }
    TextDetect td=new TextDetect();
    private int cropwidth;
    private int cropheight;
    private int backcolor;
    private String EngText;

//    public Bitmap fullblur(Bitmap cropimage){
//
//          Bitmap blurredBitmap = GaussianBlur.with(context).render(cropimage);
//         Bitmap scaledimage = Bitmap.createScaledBitmap(blurredBitmap,cropimage.getWidth(), cropimage.getHeight(), true);
//
//         return scaledimage;
//    }


    private Bitmap setBackgroundGlow(Bitmap src, int gcolor)
    {
// An added margin to the initial image
        int margin = 24;
        int halfMargin = margin / 2;
        // the glow radius
        int glowRadius = 40;

        // the glow color
        int glowColor = gcolor;

        // The original image to use


        // extract the alpha from the source image
        Bitmap alpha = src.extractAlpha();

        // The output bitmap (with the icon + glow)
        Bitmap bmp =  Bitmap.createBitmap(src.getWidth() + margin, src.getHeight() + margin, Bitmap.Config.ARGB_8888);

        // The canvas to paint on the image
        Canvas canvas = new Canvas(bmp);

        Paint paint = new Paint();
        paint.setColor(glowColor);

        // outer glow
        paint.setMaskFilter(new BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.OUTER));//For Inner glow set Blur.INNER
        canvas.drawBitmap(alpha, halfMargin, halfMargin, paint);

        // original icon
        canvas.drawBitmap(src, halfMargin, halfMargin, null);

       return  bmp;


    }

    public void colorpic(final String urdutxt){
                td.setUrduText(urdutxt);
        final Bitmap ibitmap=getbitmap();
        Palette.from(ibitmap).generate(new Palette.PaletteAsyncListener() {
            /**
             * Called when the {@link Palette} has been generated.
             *
             * @param palette
             */
            @Override
            public void onGenerated(Palette palette) {

                int defaultcolor = 0x000000;
                int bkc = palette.getDominantColor(defaultcolor);// background color
                int txc = palette.getLightVibrantColor(defaultcolor); //foreground color
                int white = 0xffffff;


                //making content with static color
                int width = ibitmap.getWidth();
                int height = ibitmap.getHeight();
                Bitmap staticbitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(staticbitmap);

                setBackcolor(bkc);
                setCropwidth(width);
                setCropheight(height);

                Paint paint = new Paint();
                Paint p=new Paint();

                if(getBackCheck()){
                    paint.setColor(bkc);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawPaint(paint);
                }


                if(ColorUtils.calculateLuminance(bkc) > 0.5){
                    p.setColor(Color.WHITE);
                    paint.setColor(Color.BLACK);
                }
                else{
                    p.setColor(Color.BLACK);
                    paint.setColor(Color.WHITE);
                }

                paint.setAntiAlias(true);
                paint.setTextSize(40.f);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(4);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                canvas.drawText(urdutxt, (width / 2.f) , (height / 2.f), paint);

                p.setAntiAlias(true);
                p.setTextSize(40.f);
                p.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(urdutxt, (width / 2.f) , (height / 2.f), p);

                //making content with blur background
//                Bitmap blurbitmap=fullblur(getbitmap());
//                Canvas cs=new Canvas(blurbitmap);
//                Log.d("Text on image","Draming Now");
//                cs.drawText(urdutxt, (width / 2.f) , (height / 2.f), paint);


                //SaveNode(setBackgroundGlow(blurbitmap,bkc));
               SaveNode(setBackgroundGlow(staticbitmap,bkc));




                /*Intent ii=new Intent(context.getApplicationContext(),ARDisplay.class);
                ii.putExtra("Eng",getEngText());
                ii.putExtra("Urdu",getUrdutext());
                ii.putExtra("bkc", Integer.toString(getBackcolor()));

                context.startActivity(ii);*/

            }
        });
    }
/*Props p=new Props();

    public Bitmap MakeSaveAndShareContent(){


                int w = p.getCropwidth();
                int h = p.getCropheight();
                int backcolor = p.getBackcolor();


                Bitmap staticbitmap = Bitmap.createBitmap(w, h * 3, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(staticbitmap);


                Paint paint = new Paint();
                Paint p = new Paint();

                paint.setColor(backcolor);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPaint(paint);


                if (ColorUtils.calculateLuminance(backcolor) > 0.5) {
                    p.setColor(Color.WHITE);
                    paint.setColor(Color.BLACK);
                } else {
                    p.setColor(Color.BLACK);
                    paint.setColor(Color.WHITE);
                }


                String[] Engtoknz = td.getEngText().split(" ");
                String finalEng = "";
                for (int i = 0; i < Engtoknz.length; i = i + 4) {
                    finalEng = Engtoknz[i] + Engtoknz[i + 1] + Engtoknz[i + 2] + Engtoknz[i + 3] + "\n";
                }
                String[] Urdutoknz = td.getUrduText().split(" ");
                String finalUrdu = "";
                for (int i = 0; i < Engtoknz.length; i = i + 4) {
                    finalUrdu = Engtoknz[i] + Engtoknz[i + 1] + Engtoknz[i + 2] + Engtoknz[i + 3] + "\n";
                }


                String urdutxt = finalEng + "\n" + finalUrdu;

                paint.setAntiAlias(true);
                paint.setTextSize(40.f);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(4);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                canvas.drawText(urdutxt, 10, 10, paint);

                p.setAntiAlias(true);
                p.setTextSize(40.f);
                p.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(urdutxt, 10, 10, p);

        return staticbitmap;
    }*/

    public void SaveNode(Bitmap bm){

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
            Toast.makeText(context.getApplicationContext(), "AR Content Save Failed", Toast.LENGTH_SHORT).show();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context.getApplicationContext(), "AR Content Save Failed", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(context.getApplicationContext(), "AR Trackable Save Failed", Toast.LENGTH_SHORT).show();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context.getApplicationContext(), "AR Tranckable Save Failed", Toast.LENGTH_SHORT).show();
            return;
        }



    }

    private String urdutext;


    Bitmap cropbitmap;
    public void setbitmap(Bitmap bitmap){
        cropbitmap=bitmap;
    }
    public Bitmap getbitmap(){
        return cropbitmap;
    }




    public String getUrdutext() {
        return urdutext;
    }

    public void setUrdutext(String urdutext) {
        this.urdutext = urdutext;
    }

    public Boolean getBackCheck() {
        return BackCheck;
    }

    public void setBackCheck(Boolean backCheck) {
        BackCheck = backCheck;
    }

    public int getCropwidth() {
        return cropwidth;
    }

    public void setCropwidth(int cropwidth) {
        this.cropwidth = cropwidth;
    }

    public int getCropheight() {
        return cropheight;
    }

    public void setCropheight(int cropheight) {
        this.cropheight = cropheight;
    }

    public int getBackcolor() {
        return backcolor;
    }

    public void setBackcolor(int backcolor) {
        this.backcolor = backcolor;
    }

    public String getEngText() {
        return EngText;
    }

    public void setEngText(String engText) {
        EngText = engText;
    }
}
