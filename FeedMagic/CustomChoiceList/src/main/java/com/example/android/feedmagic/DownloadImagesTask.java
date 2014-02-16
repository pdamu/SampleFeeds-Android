package com.example.android.feedmagic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by pdamu on 2/11/14.
 */
public class DownloadImagesTask extends AsyncTask<ImageView, Void, Bitmap> {

    ImageView imageView = null;
    LruCache<String, Bitmap> mBitmapHashMap;

    public DownloadImagesTask(LruCache<String, Bitmap> bitmapHashMap) {
        super();
        mBitmapHashMap = bitmapHashMap;
    }

    @Override
    protected Bitmap doInBackground(ImageView... imageViews) {
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        this.imageView = imageViews[0];
        return download_Image((String) imageView.getTag());
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (result != null) {
            mBitmapHashMap.put((String)imageView.getTag(), result);
            imageView.setImageBitmap(result);
        }
    }


    private Bitmap download_Image(String url) {
        Bitmap bitmap = null;
        try {
            File file = new File("data/data/com.example.android.feedmagic/feedimg-" + url.hashCode());
            if (file.exists()) {
                try {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                FileOutputStream out = new FileOutputStream(file);
                try {
                    bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    out.flush();
                    out.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
