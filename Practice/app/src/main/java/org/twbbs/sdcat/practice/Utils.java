package org.twbbs.sdcat.practice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by chunwei on 2015/7/20.
 */
public class Utils {

    public static void writeFile(Context context, String text, String fileName){
        try{
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_APPEND);
            fos.write(text.getBytes());
            fos.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String readFile(Context context, String fileName){
        try{
            FileInputStream fis = context.openFileInput(fileName);
            byte[] buffer = new byte[1024];
            fis.read(buffer);
            fis.close();
            return new String(buffer);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return "";
    }

    public static byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return  baos.toByteArray();
    }

    public static byte[] uriToBytes(Context context, Uri uri) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len = 0;

            while( (len = is.read(buffer) ) != -1 ) {
                baos.write(buffer);
            }

            return baos.toByteArray();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public static Uri getOutputUri() {
        File dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        if(dir.exists() == false) {
            dir.mkdirs();
        }
        File file = new File(dir, "photo.png");
        return Uri.fromFile(file);
    }

    public static String fetchUrl(String urlStr) {
        return new String(fetchUrlToByte(urlStr));

    }

    public static byte[] fetchUrlToByte(String urlStr) {
        try {
            URL url = new URL(urlStr);
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len = 0;

            while( (len = is.read(buffer) ) != -1 ) {
                baos.write(buffer);
            }
            return baos.toByteArray();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap byteToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    final static String GEO_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    public static String getGeoQueryUrl(String address) {
        try {
            return GEO_URL + URLEncoder.encode(address, "utf-8");
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    final static String STATIC_MAP_URL =
            "http://maps.googleapis.com/maps/api/staticmap?center=[add]&zoom=16&scale=1&size=300x300&maptype=roadmap&format=png&visual_refresh=true&markers=size:mid%7Ccolor:0xff0000%7Clabel:L%7C[add]";
    public static String getStaticMapUrl(String address) {
        try {
            String center = URLEncoder.encode(address, "utf-8");
            return STATIC_MAP_URL.replace("[add]", center);
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class NetworkTask extends AsyncTask<String, Void, byte[]>{

        private Callback callback;

        public void setCallback(Callback callback) {
            this.callback = callback;
        }

        //doInBackground的參數型別跟AsyncTask的第一個參數相同
        @Override
        protected byte[] doInBackground(String... params){
            String url = params[0];
            //String fetchResult = Utils.fetchUrl(url);
            byte[] fetchResult = Utils.fetchUrlToByte(url);
            //Log.d("debug", fetchResult);
            return fetchResult;
        }

        @Override
        protected void onPostExecute(byte[] fetchResult) {
            callback.done(fetchResult);
        }

        interface Callback {
            void done(byte[] fetchResult);
        }
    }

}
