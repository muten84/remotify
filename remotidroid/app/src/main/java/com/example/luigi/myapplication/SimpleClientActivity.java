package com.example.luigi.myapplication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Luigi on 27/04/2015.
 */
public class SimpleClientActivity extends Activity{
    private Socket client;
    private FileInputStream fileInputStream;
    private BufferedInputStream bufferedInputStream;
    private OutputStream outputStream;
    private Button button;
    private Button button2;
    private TextView text;
    private FetchImageAndSend currentTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        button = (Button) findViewById(R.id.button1);   //reference to the send button
        button2 = (Button)findViewById(R.id.button2);
        text = (TextView) findViewById(R.id.textView1);   //reference to the text view

        //Button press event listener
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                currentTask=  new FetchImageAndSend();
                currentTask.execute(SimpleClientActivity.this);

            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           currentTask.stop();
                                       }
                                   }
        );

    }

    private class FetchImageAndSend extends AsyncTask<Activity,Double,Boolean> {
        private boolean started = true;

        public void stop(){
            started = false;
        }
        @Override
        protected Boolean doInBackground(Activity... params) {
            Log.i("FetchImageAndSend", "doInBackground...");
            final Activity currentActivity = params[0];

            //Log.i("FetchImageAndSend", "screenshot taken: ");
            Socket client = null;
            try {
                client = new Socket("192.168.90.76", 4444);
            } catch (Exception e) {
                e.printStackTrace();
            }
            final Socket finalClient = client;
            Log.i("FetchImageAndSend", "connected");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int prevLength = 0;
                    while (started) {
                        //create a byte array to file

                        Bitmap image = SimpleClientActivity.this.takeScreenShot(currentActivity);
                        ByteArrayOutputStream output = new ByteArrayOutputStream(image.getByteCount());
                        image.compress(Bitmap.CompressFormat.JPEG, 1, output);
                        byte[] mybytearray = new byte[(int) output.toByteArray().length];
                        mybytearray = output.toByteArray();

                            /*if(prevLength==mybytearray.length){
                                prevLength=0;
                                continue;

                            }*/
                        prevLength = mybytearray.length;
                        OutputStream outputStream = null;
                        try {
                            outputStream = finalClient.getOutputStream();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.i("FetchImageAndSend", "going to write.... " + mybytearray.length);
                        try {
                            outputStream.write((char) '#');
                            outputStream.write((char) '#');
                            BigInteger bi = BigInteger.valueOf(mybytearray.length);
                            outputStream.write(bi.toByteArray().length);
                            outputStream.write(bi.toByteArray());
                            outputStream.write((char) '#');
                            outputStream.write(mybytearray, 0, mybytearray.length); //write file to the output stream byte by byte
                            Log.i("FetchImageAndSend", "written");
                            outputStream.flush();
                            //outputStream.close();
                            //client.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                        try {
                            Thread.sleep(1000/60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    started = false;
                    if(outputStream!=null) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(finalClient!=null) {
                        try {
                            finalClient.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.i("FetchAndSend", "Connection closed");
                }
            }).start();


            return true;

        }

        private void savePic(Bitmap b, String file) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                if (fos != null) {
                    b.compress(Bitmap.CompressFormat.JPEG, 0, fos);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public Bitmap takeScreenShotFromCanvas(Activity activity){
        activity = SimpleClientActivity.this;
        View v = activity.getWindow().getDecorView();
        Bitmap bm = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_4444);
        return bm;
    }

    public Bitmap takeScreenShot(Activity activity) {
        activity = SimpleClientActivity.this;
        View v = activity.getWindow().getDecorView();
        v.setDrawingCacheEnabled(true);
        v.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        v.buildDrawingCache();
        Bitmap b1 = v.getDrawingCache();
        return b1;
    }
}
