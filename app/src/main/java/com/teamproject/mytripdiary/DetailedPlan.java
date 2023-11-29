package com.teamproject.mytripdiary;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DetailedPlan extends AppCompatActivity {

    private ImageButton btn_home;
    private MyDatabase myDatabase;
    private TextView textView3;
    LinearLayout layoutPlans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_plan);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String startdate = intent.getStringExtra("startdate");
        String enddate = intent.getStringExtra("enddate");

        textView3 = findViewById(R.id.textView3);
        textView3.setText(title);

                myDatabase = new MyDatabase(this);
        myDatabase.open();

        btn_home = findViewById(R.id.btn_home);
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent go_home = new Intent(DetailedPlan.this , MainActivity.class);
                startActivity(go_home);



            }
        });
    }
}