package org.twbbs.sdcat.practice;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
//import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends ActionBarActivity {

    private EditText editText;
    private CheckBox hideCheckBox;
    private ListView listView;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sp = getSharedPreferences("settings", Context.MODE_PRIVATE);
        editor = sp.edit();

        listView = (ListView) findViewById(R.id.listView);

        //先繪出畫面後(setContentView)，才能從目前的畫面中取得物件實体。
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
        hideCheckBox= (CheckBox) findViewById(R.id.checkBox);
        // R -> 代表所有resource裡的物件
        //id -> Layout 或 menu 裡定義的id
        hideCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("checkbox",isChecked);
            }
        });
        hideCheckBox.setChecked(sp.getBoolean("checkbox", false));

        loadHistory();
    }

    //onClick需為public , Arg需有View
    public void submit(View view){
        String text= editText.getText().toString();
        //Log.d("debug", "edittext: " + text);
        //duration : 一個常數, 設定不同的顯示時間 0 SHORT, 1 LONG

        if(hideCheckBox.isChecked()) {
            text = "********";
        }
        Utils.writeFile(this, text + "\n", "history.txt");

        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        editText.setText("");

        loadHistory();
        //TextView textView = (TextView) findViewById(R.id.textView);
        //textView.setText(Utils.readFile(this, "history.txt"));
    }

    private void loadHistory(){
        String history = Utils.readFile(this,"history.txt");
        String[] data = history.split("\n");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,data);
        listView.setAdapter(adapter);
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
