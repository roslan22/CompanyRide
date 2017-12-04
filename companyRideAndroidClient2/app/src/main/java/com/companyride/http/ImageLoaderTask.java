package com.companyride.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;
import com.companyride.utils.UtilityFunctions;

import java.io.*;
import java.net.URL;

/**
 * Created by Vlada on 28/03/2015.
 */
public class ImageLoaderTask extends AsyncTask<String, Void, Bitmap>
{
    ImageView bmImage;
    Context   mContext;
    String    id;

    public ImageLoaderTask(ImageView bmImage, Context context, String id) {
        this.bmImage = bmImage;
        this.mContext= context;
        this.id      = id;
    }

    public static byte[] convertFileImageToByteArray(String filePath){
        Bitmap bm = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        return b;
    }

    public static Bitmap getScaledBitmapFromFile(String filePath, ImageView destView) {
        // Get the dimensions of the View
        int targetW = destView.getWidth();
        int targetH = destView.getHeight();

        if(targetH  < 213) targetH = 213;
        if(targetW < 236) targetW = 236;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmOptions);
        return bitmap;
    }

    public static void saveStreamToFile(InputStream in, File file){
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File createImageFile(String imageFileName, Context context) throws IOException {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            UtilityFunctions.showMessageInToast(context, "Cannot find mounted external storage!");
            return null;
        }
        File storageDir = context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES);
        File fullPathFile = new File(storageDir + "/" + imageFileName + ".jpg");
//        if (fullPathFile.exists())
//        {
//            fullPathFile.delete();
//        }
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );

        return fullPathFile;
    }

    @Override
    protected Bitmap doInBackground(String... address)
    {
        Bitmap bmp = null;
        try
        {
            URL url = new URL(address[0]);
            File file = ImageLoaderTask.createImageFile(id, mContext);
            if (!file.exists())
                ImageLoaderTask.saveStreamToFile(url.openConnection().getInputStream(), file);
            bmp = ImageLoaderTask.getScaledBitmapFromFile(file.getPath(), bmImage);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println("Failed to load image: " + ex.getMessage());
        }

        return bmp;
    }

    protected void onPostExecute(Bitmap result) {
        if (result != null)
            bmImage.setImageBitmap(result);
    }
}

