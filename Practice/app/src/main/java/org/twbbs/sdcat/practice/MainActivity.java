package org.twbbs.sdcat.practice;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private EditText editText;
    private CheckBox hideCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //��ø�X�e����(setContentView)�A�~��q�ثe���e�������o������^�C
        editText = (EditText) findViewById(R.id.editText);
        //editText.setText("Enter New Text Here.");
        hideCheckBox= (CheckBox) findViewById(R.id.checkBox);
    }

    //onClick�ݬ�public , Arg�ݦ�View
    public void submit(View view){
        String text= editText.getText().toString();
        //Log.d("debug", "edittext: " + text);
        //duration : �@�ӱ`��, �]�w���P����ܮɶ� 0 SHORT, 1 LONG

        if(hideCheckBox.isChecked()) {
            text = "********";
        }
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        editText.setText("");
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
