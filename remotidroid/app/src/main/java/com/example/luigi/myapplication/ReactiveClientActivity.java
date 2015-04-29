package com.example.luigi.myapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;

/**
 * Created by Luigi on 29/04/2015.
 */
public class ReactiveClientActivity extends Activity {

    private Button button;
    private Button button2;
    private TextView text;
    private Handler mHandler;
    private Socket client = null;
    private WebView webview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        button = (Button) findViewById(R.id.button1);   //reference to the send button
        button2 = (Button) findViewById(R.id.button2);
        text = (TextView) findViewById(R.id.textView1);   //ref
        webview = (WebView) findViewById(R.id.webview);
        WebSettings s = webview.getSettings();
        s.setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        webview.loadUrl("http://www.luigibifulco.it/blog");
        mHandler = new Handler();

        button2.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                                new CloseConnectionTask().execute();
                                       }
                                   }
        );
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    new ConnectTask(new Callback<Boolean>() {
                        @Override
                        public void call(Boolean data) {
                            mHandler.postDelayed(new TakeScreenUIRunnable(new Callback<Bitmap>() {
                                @Override
                                public void call(Bitmap data) {
                                    new SendSceenShotTask(new Callback<Boolean>() {
                                        @Override
                                        public void call(Boolean data) {
                                            if (!data) {
                                                button.setEnabled(true);
                                                button.setClickable(true);
                                                new CloseConnectionTask().execute(ReactiveClientActivity.this);
                                            }
                                        }
                                    }).execute(data);
                                }
                            }), 1000);
                            button.setEnabled(false);
                            button.setClickable(false);
                        }
                    }).execute(ReactiveClientActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public interface Callback<E> {
        public void call(E data);
    }

    public class CloseConnectionTask extends AsyncTask<Activity, Double, Boolean> {
        @Override
        protected Boolean doInBackground(Activity... params) {
            try {
                client.getOutputStream().close();
                client.close();
                client = null;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }
    }

    public class SendSceenShotTask extends AsyncTask<Bitmap, Double, Boolean> {

        private Callback callback;

        public SendSceenShotTask(Callback<Boolean> callback) {
            this.callback = callback;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }

        @Override
        protected Boolean doInBackground(Bitmap... params) {
            try {
                OutputStream outputStream = client.getOutputStream();
                Bitmap image = params[0];
                ByteArrayOutputStream output = new ByteArrayOutputStream(image.getByteCount());
                image.compress(Bitmap.CompressFormat.JPEG, 40, output);
                byte[] mybytearray = new byte[(int) output.toByteArray().length];
                mybytearray = output.toByteArray();
                try {
                    outputStream.write((char) '#');
                    outputStream.write((char) '#');
                    BigInteger bi = BigInteger.valueOf(mybytearray.length);
                    outputStream.write(bi.toByteArray().length);
                    outputStream.write(bi.toByteArray());
                    outputStream.write((char) '#');
                    outputStream.write(mybytearray, 0, mybytearray.length); //write file to the output stream byte by byte
                    Log.i("SendSceenShotTask", "written");
                    outputStream.flush();
                    //outputStream.close();
                    //client.close();
                } catch (Exception e) {
                    //e.printStackTrace();
                    return false;
                }
            } catch (Exception e) {
                //e.printStackTrace();
                return false;
            }
            return true;
        }
    }

    public class ConnectTask extends AsyncTask<Activity, Double, Boolean> {
        Callback<Boolean> callback;

        public ConnectTask(Callback<Boolean> callback) {
            this.callback = callback;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (callback != null) {
                callback.call(aBoolean);
            }
        }

        @Override
        protected Boolean doInBackground(Activity... params) {

            try {
                client = new Socket("192.168.90.76", 4444);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }
    }

    private class TakeScreenUIRunnable implements Runnable {
        private Callback<Bitmap> callback;

        public TakeScreenUIRunnable(Callback<Bitmap> callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            Bitmap bm = takeScreenShot(ReactiveClientActivity.this);
            this.callback.call(bm);
            mHandler.postDelayed(this, 10);
        }
    }

    public Bitmap takeScreenShot(Activity activity) {
        activity = ReactiveClientActivity.this;
        View v = activity.getWindow().getDecorView();
        v.setDrawingCacheEnabled(true);
        v.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        v.buildDrawingCache();
        Bitmap b1 = v.getDrawingCache();
        //v.setDrawingCacheEnabled(false);
        return b1;
    }
}
