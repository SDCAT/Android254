package org.twbbs.sdcat.practice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OrderDetialActivity extends ActionBarActivity implements OnMapReadyCallback {

    private TextView textView;
    private WebView webView;
    private ImageView imageView;
    private ListView listView;

    private List<ParseObject> menuQueryReslut;

    private double[] location;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detial);
        listView = (ListView) findViewById(R.id.listView_drink);

        textView = (TextView) findViewById(R.id.textView);
        webView = (WebView) findViewById(R.id.webView);
        imageView = (ImageView) findViewById(R.id.imageView);


        final Intent intent = getIntent();
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
        //Log.d("debug", staticMapUrl);
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

        loadDrinks(parseid);

    }


    private void loadDrinks(String parseid) {
        textView.setText(parseid);
        //final List<Map<String, String>> data = new ArrayList<>();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Order");
        query.whereEqualTo("objectId", parseid);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e == null){
                    //textView.setText("search OK");
                    List<Map<String, String>> data = new ArrayList<>();
                    JSONArray drinks = parseObject.getJSONArray("menu");
                    textView.setText(drinks.toString());

                    try{
                        int count = drinks.length();
                        for(int i = 0; i < count; i ++){
                            JSONObject drinkObj = drinks.getJSONObject(i);
                            String drinkType = drinkObj.getString("name");
                            String l = String.valueOf(drinkObj.getInt("l"));
                            String m = String.valueOf(drinkObj.getInt("m"));
                            String s = String.valueOf(drinkObj.getInt("s"));
                            Log.d("debug",drinkObj.toString());

                            Map<String, String> item = new HashMap<>();
                            item.put("drinkType", drinkType);
                            item.put("smallCount", s);
                            item.put("mediumCount", m);
                            item.put("largeCount", l);

                            data.add(item);
                        }
                        setDataToListView(data);
                    }
                    catch (JSONException ee){
                        Log.d("debug",ee.getMessage());
                    }

                }
            }
        });
    }
    private void setDataToListView(List<Map<String, String>> dataList) {
        //form的key依序對應到to的id, 陣列數量會相等
        String[] from = new String[]{"drinkType", "smallCount", "mediumCount", "largeCount"};
        int[] to = new int[]{R.id.listview_item_drinkType, R.id.listview_item_smallCount,
                R.id.listview_item_mediumCount, R.id.listview_item_largeCount};

        SimpleAdapter adapter = new SimpleAdapter(this, dataList, R.layout.listview_menu, from, to);
        listView.setAdapter(adapter);

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
