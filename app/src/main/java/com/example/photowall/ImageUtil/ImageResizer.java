package com.example.photowall.ImageUtil;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.example.photowall.Widget.SquareImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ImageResizer {
    public ImageResizer(){

    }

    public static Bitmap decodeSampledBitmapFromStream(Activity ac, String url) throws IOException {
        Uri uri = ImageFolder.getMediaUriFromPath(ac,url);
        InputStream input = ac.getContentResolver().openInputStream(uri);
        if(input == null)
            return null;
        else {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input,null,options);
            input.close();
            int photoWidth = options.outWidth;
            int photoHeight = options.outHeight;
            WindowManager wm = ac.getWindowManager();
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;
            float density = dm.density;
            int reqWidth = (int) (width/3);
            if(photoWidth == -1 || photoHeight == -1)
                return null;
            options.inSampleSize = calculateInSampleSize(options,reqWidth,reqWidth);
            options.inJustDecodeBounds = false;
            input = ac.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input,null,options);
            if(input != null)
                input.close();
            return compressImage(bitmap);
        }

    }

    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        if(reqWidth == 0 || reqHeight == 0)
            return 1;
        int inSampleSize = 1;
        final int width = options.outWidth;
        final int height = options.outHeight;
        if(width > reqWidth || height > reqHeight){
            final int halfWidth = width/2;
            final int halfHeight = height/2;
            while ((halfWidth/inSampleSize) >= reqWidth ||
                    (halfHeight/inSampleSize) >= reqHeight){
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap compressImage(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        int options = 90;
        while (baos.toByteArray().length/1024>100){
            baos.reset();
            bitmap.compress(Bitmap.CompressFormat.JPEG,options,baos);
            options -= 10;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmapC = BitmapFactory.decodeStream(bais,null,null);
        return bitmapC;
    }
}
