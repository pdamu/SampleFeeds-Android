package com.example.android.feedmagic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by pdamu on 2/11/14.
 */
public class DownloadImagesTask extends AsyncTask<ImageView, Void, Bitmap> {

    ImageView imageView = null;

    @Override
    protected Bitmap doInBackground(ImageView... imageViews) {
        this.imageView = imageViews[0];
        return download_Image((String)imageView.getTag());
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if(result != null) {
            imageView.setImageBitmap(result);
        }
    }


    private Bitmap download_Image(String url) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    }