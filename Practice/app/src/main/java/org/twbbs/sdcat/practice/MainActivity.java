package org.twbbs.sdcat.practice;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
//import android.util.Log;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_CODE_MENU_ACTIVITY = 1;
    private EditText editText;
    private CheckBox hideCheckBox;
    private ListView listView;
    private Spinner spinner;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String menuResult;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "yEbzCH4YgT2LyUAinwI3psNyxw21etL2BCqzZXOy", "oyJa4cS4p239eC1e19JbIG8AI17zSosSxrko2nlH");

        //ParseObject testObject = new ParseObject("TestObject");
        //testObject.put("foo", "bar");
        //testObject.saveInBackground();

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

        loadHistory();
        loadStoreInfo();
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
                //orderObject.saveInBackground();
                orderObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d("debfg", "after done()");
                    }
                });
                Log.d("debug", "after SaveInBackground");

//                Utils.writeFile(this, order.toString() + "\n", "history.txt");
//                //Utils.writeFile(this, text + "\n", "history.txt");

                Toast.makeText(this, order.toString(), Toast.LENGTH_LONG).show();
                //Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                editText.setText("");

                menuResult = null;
                loadHistory();

            }
            catch (JSONException e) {
                e.printStackTrace();
            }

        }

        //TextView textView = (TextView) findViewById(R.id.textView);
        //textView.setText(Utils.readFile(this, "history.txt"));
    }

    public void goToOrderDetialActivity(View view, int position) {
        Intent intent = new Intent();
        intent.setClass(this, OrderDetialActivity.class);
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
                    for (ParseObject object : list) {
                        String note = object.getString("note");
                        String sum = getDrindSum(object.getJSONArray("menu"));
                        String address = object.getString("address");

                        Map<String, String> item = new HashMap<>();
                        item.put("note", note);
                        item.put("sum", sum);
                        item.put("address", address);

                        data.add(item);
                    }
                    setDataToListView(data);
                }
            }
        });
    }

    private void setDataToListView(List<Map<String, String>> data) {
        //form的key依序對應到to的id, 陣列數量會相等
        String[] from = new String[]{"note", "sum", "address"};
        int[] to = new int[]{R.id.listview_item_note, R.id.listview_item_sum,
                R.id.listview_item_address};

        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.listview_item, from, to);
        listView.setAdapter(adapter);
    }

    public String getDrindSum(JSONArray menu) {
        return "77";
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
        switch (requestCode){
            case REQUEST_CODE_MENU_ACTIVITY: {
                if(resultCode == RESULT_OK) {
                    //TODO
                    menuResult = data.getStringExtra("result");
                    Toast.makeText(this, menuResult, Toast.LENGTH_LONG).show();
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
        }

        return super.onOptionsItemSelected(item);
    }
}
