package org.twbbs.sdcat.practice;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;


public class OrderDetialActivity extends ActionBarActivity {

    private TextView textView;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detial);

        textView = (TextView) findViewById(R.id.textView);
        webView = (WebView) findViewById(R.id.webView);

        Intent intent = getIntent();
        String note = intent.getStringExtra("note");
        String address = intent.getStringExtra("address").split(" ◎ ")[1];

        String sum = intent.getStringExtra("sum");
        String parseid = intent.getStringExtra("pid");

        //Toast.makeText(this, note + "," + address + "," + sum + "," + parseid + "," + result, Toast.LENGTH_LONG).show();
        webView.loadUrl(Utils.getStaticMapUrl(address));

        String url = Utils.getGeoQueryUrl(address);
        Utils.NetworkTask networkTask = new Utils.NetworkTask();
        networkTask.setCallback(new Utils.NetworkTask.Callback() {
            @Override
            public void done(String fetchResult) {
                //textView.setText(fetchResult);
            }
        });
        networkTask.execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_order_detial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
