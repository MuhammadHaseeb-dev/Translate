package com.xlsoft.kudanar.TextDetect;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import static android.content.Context.MODE_PRIVATE;


/**
 * Created by Muhammad on 15/02/2018.
 */

public class TextDetect {

    TextRecognizer recognizer;
    Context context;

    private String EngText;
private String UrduText;

    public TextDetect(Context current){
        context=current;
    }

    public TextDetect(){

    }


    public Bitmap croping(Bitmap original, Display d, Rect box){

        int fwidth = box.width();
        int fheight = box.height()+50;
        int fleft = box.left;
        int ftop = box.top-50;
        int sh=getStatusBarHeight();
        int screenwidth = d.getWidth();
        int screenheight = d.getHeight();
        int weight1of10=(screenheight-sh)/10;
        int frameheight=screenheight-weight1of10-sh;


        Bitmap scaledimage = Bitmap.createScaledBitmap(original,frameheight, screenwidth, true);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        Bitmap rotatedimage = Bitmap.createBitmap(scaledimage, 0, 0, scaledimage.getWidth(), scaledimage.getHeight(), matrix, true);

        //  int leftpx = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32,r.getDisplayMetrics()));

     //  int nt=ftop-weight1of10-sh;
        Bitmap cropimage= Bitmap.createBitmap(rotatedimage,fleft ,ftop+50 ,fwidth,fheight-50);

      setTrackableimage(Bitmap.createBitmap(rotatedimage,fleft ,ftop+sh ,fwidth,fheight));
       return cropimage;

    }

private Bitmap trackableimage;


    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public String recognize(Bitmap cropimage){
        try{
            String Lines = "";
            String words = "";
            String blocks = "";

            recognizer = new TextRecognizer.Builder(context.getApplicationContext()).build();
            if (recognizer.isOperational() && cropimage != null) {
                Frame frame = new Frame.Builder().setBitmap(cropimage).build();
                SparseArray<TextBlock> textBlockSparseArray = recognizer.detect(frame);
                for (int i = 0; i < textBlockSparseArray.size(); i++) {
                    TextBlock textBlock = textBlockSparseArray.valueAt(i);
                    blocks = blocks + textBlock.getComponents().toString() + "\n";
                    for (Text line : textBlock.getComponents()) {
                        Lines = Lines + line.getComponents().toString();
                        for (Text element : line.getComponents()) {
                            words = words + element.getValue() + " ";
                        }
                        //words=words+"\n";
                    }
                    words=words+"\n";

                }

                if (textBlockSparseArray.size() == 0) {
                                       //txt_view.setText("Scan Failed: Found nothing to scan");
                    return "";
                } else {
                    preAR p=new preAR();

                    p.setEngText(words.toString());
                     Log.d("Lines",Lines);

                    SharedPreferences.Editor editor = context.getSharedPreferences("admin", MODE_PRIVATE).edit();
                    editor.putString("eng", words);
                    editor.commit();

                    return words + "\n";


                    //txt_view.setText(Props + "\n");
                }
            }
        } catch (Exception ex) {
             return "";

        }
        return "";
    }

    public Bitmap getTrackableimage() {
        return trackableimage;
    }

    public void setTrackableimage(Bitmap trackableimage) {
        this.trackableimage = trackableimage;
    }

    private Boolean tokenstatus;

    public Boolean getTokenstatus() {
        return tokenstatus;
    }

    public void setTokenstatus(Boolean tokenstatus) {
        this.tokenstatus = tokenstatus;
    }

    public String getEngText() {
        return EngText;
    }

    public void setEngText(String engText) {
        EngText = engText;
    }

    public String getUrduText() {
        return UrduText;
    }

    public void setUrduText(String urduText) {
        UrduText = urduText;
    }
}
