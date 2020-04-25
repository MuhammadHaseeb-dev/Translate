package numl.fyp.ashs.ltu_new.utils;

import android.graphics.Bitmap;

public class ImageOps {



    public Bitmap getFullScreenImage(Bitmap bitmap,int width,int height){

        return Bitmap.createScaledBitmap(bitmap,width,height,true);

    }

    public Bitmap getCropedImage(Bitmap bitmap,int left,int top,int width, int height){

        return Bitmap.createBitmap(bitmap,left,top,width,height);
    }

}
