package com.cute.meido;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.cute.meido.utils.ToolBox;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        findViewById(R.id.button4561).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView getStance = (TextView)findViewById(R.id.getdistance);

                SharedPreferences.Editor editor = getSharedPreferences("settings",MODE_PRIVATE).edit();
                editor.putInt("distance",Integer.valueOf(getStance.getText().toString()));
                editor.commit();
            }
        });
    }
}
