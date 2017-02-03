package com.cute.meido;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        findViewById(R.id.button4561).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
                builder.setTitle("test 设置 标题");
                //builder.setMessage(" test 设置消息");
                final String[] dateSouce = new String[] {"1a","2b","3c","4d"};

                final String test = "";
                builder.setMultiChoiceItems(dateSouce, new boolean[]{false,false,false,false}, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {


                    }
                });
                builder.show();
            }
        });
    }
}
