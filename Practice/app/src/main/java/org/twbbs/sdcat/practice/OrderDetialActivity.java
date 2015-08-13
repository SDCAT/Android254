package org.twbbs.sdcat.practice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class OrderDetialActivity extends ActionBarActivity implements OnMapReadyCallback {

    private TextView textView;
    private WebView webView;
    private ImageView imageView;

    private double[] location;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detial);

        textView = (TextView) findViewById(R.id.textView);
        webView = (WebView) findViewById(R.id.webView);
        imageView = (ImageView) findViewById(R.id.imageView);

        Intent intent = getIntent();
        String note = intent.getStringExtra("note");
        String address = intent.getStringExtra("address").split(" ◎ ")[1];

        String sum = intent.getStringExtra("sum");
        String parseid = intent.getStringExtra("pid");

        //Toast.makeText(this, note + "," + address + "," + sum + "," + parseid + "," + result, Toast.LENGTH_LONG).show();
        //webView.loadUrl(Utils.getStaticMapUrl(address));

        String url = Utils.getGeoQueryUrl(address);
        Utils.NetworkTask networkTask = new Utils.NetworkTask();
        networkTask.setCallback(new Utils.NetworkTask.Callback() {
            @Override
            public void done(byte[] fetchResult) {
                //textView.setText(new String(fetchResult));
                String result = new String(fetchResult);
                location = Utils.getGeoPoint(result);
                if(googleMap != null) {
                    setUpGoogleMap();
                }
            }
        });
        networkTask.execute(url);

        String staticMapUrl = Utils.getStaticMapUrl(address);
        Log.d("debug", staticMapUrl);
        Utils.NetworkTask getStaticMapTask = new Utils.NetworkTask();
        getStaticMapTask.setCallback(new Utils.NetworkTask.Callback() {
            @Override
            public void done(byte[] fetchResult) {
                Bitmap bm = Utils.byteToBitmap(fetchResult);
                imageView.setImageBitmap(bm);
            }
        });
        getStaticMapTask.execute(staticMapUrl);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

    }

    private void setUpGoogleMap() {
        String[] tmp = getIntent().getStringExtra("address").split(" ◎ ");
        String title = tmp[0];
        String address = tmp[1];
        LatLng latLng = new LatLng(location[0],location[1]);
        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        googleMap.addMarker(new MarkerOptions().position(latLng).title(title).snippet(address));
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        if(location != null) {
            setUpGoogleMap();
        }
    }
}
