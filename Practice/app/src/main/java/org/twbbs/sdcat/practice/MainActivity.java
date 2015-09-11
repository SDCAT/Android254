package org.twbbs.sdcat.practice;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_CODE_MENU_ACTIVITY = 1;
    private static final int REQUEST_CODE_CAMERA = 2;
    private EditText editText;
    private CheckBox hideCheckBox;
    private ListView listView;
    private Spinner spinner;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String menuResult;
    private List<ParseObject> orderQueryReslut;
    private Bitmap bitmap;

    private boolean hasPhoto = false;
    private CallbackManager callbackManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "yEbzCH4YgT2LyUAinwI3psNyxw21etL2BCqzZXOy", "oyJa4cS4p239eC1e19JbIG8AI17zSosSxrko2nlH");
        //Parse.initialize(this, "6As46KZTL6DzHlA0YrdQcHxe2Kkb6Z7guxjqH86f", "77G3RUogihUrOHAsIFxOFsd1O98R79mPAxHWsBbo");

        //ParseObject testObject = new ParseObject("TestObject");
        //testObject.put("foo", "bar");
        //testObject.saveInBackground();

        //hw4();

        sp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sp.edit();

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                goToOrderDetialActivity(view, position);
            }
        });

        //先繪出畫面後(setContentView)，才能從目前的畫面中取得物件實体。
        spinner = (Spinner) findViewById(R.id.store_name);
        editText = (EditText) findViewById(R.id.editText);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                String text = editText.getText().toString();
                editor.putString("text", text);
                editor.commit();

                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    submit(v);
                    return true;
                }
                return false;
                //keyCode問題 : sdk無法支援所有輸入法及字元、控制字元可能也不同
            }
        });

        editText.setText(sp.getString("text",""));

        //editText.setText("Enter New Text Here.");
        hideCheckBox = (CheckBox) findViewById(R.id.checkBox);
        // R -> 代表所有resource裡的物件
        //id -> Layout 或 menu 裡定義的id
        hideCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("checkbox", isChecked);
            }
        });
        hideCheckBox.setChecked(sp.getBoolean("checkbox", false));

        setupFacebookLogin();
        loadHistory();
        loadStoreInfo();
    }

    private void hw4() {
        ParseObject pObject = new ParseObject("HomeworkParse");
        pObject.put("sid", "馬君瑋");
        pObject.put("email", "sdcatsilport@gmail.com");
        pObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d("debfg", "Upload Complect");
            }
        });
    }

    private void setupFacebookLogin() {
        final LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        // If using in a fragment
        // Other app specific specialization

        callbackManager = CallbackManager.Factory.create();
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                //Login後取得Token
                AccessToken token = loginResult.getAccessToken();
                //依據GraphPath API取得callback
                GraphRequest.newGraphPathRequest(token, "/me", new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        //JSON物件
                    }
                });
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    public void hideCheckClick(View view) {
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        if(hideCheckBox.isChecked()) {
            imageView.setVisibility(View.INVISIBLE);
        }
        else {
            imageView.setVisibility(View.VISIBLE);
        }
    }

    //onClick需為public , Arg需有View
    public void submit(View view){
        String text= editText.getText().toString();
        //Log.d("debug", "edittext: " + text);
        //duration : 一個常數, 設定不同的顯示時間 0 SHORT, 1 LONG

        if(hideCheckBox.isChecked()) {
            text = "********";
        }

        if(menuResult != null) {
            try {
                String storeInfo = spinner.getSelectedItem().toString();

                JSONObject order = new JSONObject();
                JSONArray menuResultArray = new JSONArray(menuResult);
                order.put("note", text);
                order.put("menu", menuResultArray);
                order.put("address", storeInfo);

                ParseObject orderObject = new ParseObject("Order");
                orderObject.put("note", text);
                orderObject.put("menu", menuResultArray);
                orderObject.put("address", storeInfo);

                //if(bitmap != null) {
                //    ParseFile file = new ParseFile("photo.png", Utils.bitmapToBytes(bitmap));
                //    orderObject.put("photo", file);
                //}
                if(hasPhoto) {
                    Uri uri = Utils.getOutputUri();
                    ParseFile file = new ParseFile("photo.png", Utils.uriToBytes(this,uri));
                    orderObject.put("photo", file);
                }

                //orderObject.saveInBackground();
                orderObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d("debfg", "after done()");
                        loadHistory();
                    }
                });
                Log.d("debug", "after SaveInBackground");

//                Utils.writeFile(this, order.toString() + "\n", "history.txt");
//                //Utils.writeFile(this, text + "\n", "history.txt");

                Toast.makeText(this, order.toString(), Toast.LENGTH_LONG).show();
                //Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                editText.setText("");

                menuResult = null;
                hasPhoto = false;
                //loadHistory();

            }
            catch (JSONException e) {
                e.printStackTrace();
            }

        }

        //TextView textView = (TextView) findViewById(R.id.textView);
        //textView.setText(Utils.readFile(this, "history.txt"));
    }

    private void pushDataToIntent(View view, Intent intent) {
        //透過findViewById取得
        TextView note = (TextView)view.findViewById(R.id.listview_item_note);
        TextView address = (TextView)view.findViewById(R.id.listview_item_address);
        TextView sum = (TextView)view.findViewById(R.id.listview_item_sum);
        TextView pid = (TextView)view.findViewById(R.id.listview_item_id);
        String noteStr = note.getText().toString();
        String addressStr = address.getText().toString();
        String sumStr = sum.getText().toString();
        String pidStr = pid.getText().toString();
        intent.putExtra("note", noteStr);
        intent.putExtra("address", addressStr);
        intent.putExtra("sum", sumStr);
        intent.putExtra("pid", pidStr);
    }

    private void pushDataToIntent(int position, Intent intent) {
        SimpleAdapter adapter = (SimpleAdapter) listView.getAdapter();
        Map<String, String> item = (Map<String, String>) adapter.getItem(position);
        intent.putExtra("note", item.get("note").toString());
        intent.putExtra("address", item.get("address").toString());
        intent.putExtra("sum", item.get("sum").toString());
        intent.putExtra("pid", item.get("pid").toString());
    }

    private void pushDataToIntent(ParseObject object, Intent intent) {
        intent.putExtra("note", object.getString("note"));
        intent.putExtra("address", object.getString("address"));
        intent.putExtra("sum", getDrinkSum(object.getJSONArray("menu")));
        intent.putExtra("pid", object.getObjectId());
    }

    private void goToCamera() {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Utils.getOutputUri());
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    public void goToOrderDetialActivity(View view, int position) {
        Intent intent = new Intent();
        intent.setClass(this, OrderDetialActivity.class);
        //pushDataToIntent(view, intent);
        //pushDataToIntent(position, intent);
        pushDataToIntent(orderQueryReslut.get(position), intent);

        startActivity(intent);
    }

    public void goToMenuActivity(View view){
        Intent intent = new Intent();
        intent.setClass(this, MenuActivity.class);
        //startActivity(intent);
        //REQUEST CODE : 認證/識別值是從那一個Activity傳回, 常數自行定義
        startActivityForResult(intent, REQUEST_CODE_MENU_ACTIVITY);
    }

    private void loadHistory(){
        final List<Map<String, String>> data = new ArrayList<>();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Order");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) { // e為null表示沒有異常
                    orderQueryReslut = list;
                    for (ParseObject object : list) {
                        String note = object.getString("note");
                        String sum = getDrinkSum(object.getJSONArray("menu"));
                        String address = object.getString("address");
                        String parseId = object.getObjectId();

                        Map<String, String> item = new HashMap<>();
                        item.put("note", note);
                        item.put("sum", sum);
                        item.put("address", address);
                        item.put("pid", parseId);

                        data.add(item);
                    }
                    setDataToListView(data);
                }
            }
        });
    }

    private void setDataToListView(List<Map<String, String>> data) {
        //form的key依序對應到to的id, 陣列數量會相等
        String[] from = new String[]{"note", "sum", "address", "pid"};
        int[] to = new int[]{R.id.listview_item_note, R.id.listview_item_sum,
                R.id.listview_item_address, R.id.listview_item_id};

        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.listview_item, from, to);
        listView.setAdapter(adapter);
    }

    public String getDrinkSum(JSONArray menu) {
        int count = menu.length();
        int drinksum = 0;
        for(int i = 0; i < count; i ++) {
            try {
                JSONObject drink = menu.getJSONObject(i);
                int l = drink.getInt("l");
                int m = drink.getInt("m");
                int s = drink.getInt("s");
                drinksum = drinksum + l + m + s;
            }
            catch (JSONException e) {
                e.printStackTrace();;
            }

        }
        return String.valueOf(drinksum);
        //return "77";
    }

    private void loadStoreInfo() {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("StoreInfo");
        query.orderByAscending("name");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e == null) {
                    String[] data = new String[list.size()];
                    for (int i = 0; i < list.size(); i++) {
                        String name = list.get(i).getString("name");
                        String address = list.get(i).getString("address");
                        data[i] = name + " ◎ " + address;
                    }
                    setDataToSpinner(data);
                }
            }
        });

        //String[] data = new String[]{"中山店", "中正店"};
        //String[] data = getResources().getStringArray(R.array.store_info);

        //setDataToSpinner(data);
    }

    private void setDataToSpinner(String[] data) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, data);
        spinner.setAdapter(adapter);
    }

    //右毽, Generate..., 可選Override Methods
    //這邊先找 onActivityResult, 好取得Activity回傳資料
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE_MENU_ACTIVITY: {
                if(resultCode == RESULT_OK) {
                    menuResult = data.getStringExtra("result");
                    Toast.makeText(this, menuResult, Toast.LENGTH_LONG).show();
                }
            }
                break;
            case REQUEST_CODE_CAMERA: {
                if(resultCode == RESULT_OK) {
                    //bitmap = data.getParcelableExtra("data");
                    Uri uri = Utils.getOutputUri();
                    ImageView imageView = (ImageView)findViewById(R.id.imageView);
                    //imageView.setImageBitmap(bitmap);
                    imageView.setImageURI(uri);
                    hasPhoto = true;
                    //try {
                    //    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    //}
                    //catch (IOException e) {
                    //    e.printStackTrace();
                    //}
                } else {
                    hasPhoto = false;
                }

            }
                break;
            default:
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        } else if (id == R.id.action_take_photo) {
            goToCamera();
        }

        return super.onOptionsItemSelected(item);
    }
}
