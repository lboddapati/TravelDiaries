package com.example.traveldiaries;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import java.io.File;

/**
 * to improve the resolution of photos
 */
public class ImageProcessingHelperClass {

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static int calculateInSampleSize(long actualByteSize, long reqByteSize) {
        int inSampleSize = 1;

        Log.d("calculateInSampleSize", "reqByteSize is " + reqByteSize);
        Log.d("calculateInSampleSize", "actualByteSize is " + actualByteSize);

        if (actualByteSize > reqByteSize) {

            final long halfByteSize = actualByteSize / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps the
            // bytesize larger than the requested bytesize.
            while ((halfByteSize / inSampleSize) > reqByteSize) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromByteArry(byte[] data, int reqWidth, int reqHeight) {
        Log.d("decodeSampledBitmapFromByteArry", "reqWidth is " + reqWidth);
        Log.d("decodeSampledBitmapFromByteArry", "reqHeight is " + reqHeight);

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        Log.d("decodeSampledBitmapFromByteArry", "insample size is " + options.inSampleSize);


        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        Log.d("decodeSampledBitmapFromResource", "reqWidth is " + reqWidth);
        Log.d("decodeSampledBitmapFromResource", "reqHeight is " + reqHeight);

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        Log.d("decodeSampledBitmapFromResource", "insample size is " + options.inSampleSize);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(String filepath, int reqWidth, int reqHeight) {
        Log.d("decodeSampledBitmapFromFile", "reqWidth is " + reqWidth);
        Log.d("decodeSampledBitmapFromFile", "reqHeight is " + reqHeight);

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        Log.d("decodeSampledBitmapFromFile", "insample size is " + options.inSampleSize);


        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filepath, options);
    }

    public static Bitmap decodeSampledBitmapFromFile(String filepath, long reqByteSize) {
        long actualByteSize = (new File(filepath)).length();
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(actualByteSize, reqByteSize);
        Log.d("decodeSampledBitmapFromFile", "insample size is " + options.inSampleSize);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filepath, options);
    }

    public static Bitmap getResizedBitmap(Bitmap inBitmap, int reqWidth, int reqHeight) {
        return Bitmap.createScaledBitmap(inBitmap, reqWidth, reqHeight, true);
    }

    public static int dpToPixel(Resources resource, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resource.getDisplayMetrics());
    }
}
