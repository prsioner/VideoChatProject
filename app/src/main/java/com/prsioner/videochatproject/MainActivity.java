package com.prsioner.videochatproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.prsioner.videochatproject.client.ClientAActivity;
import com.prsioner.videochatproject.server.ServerBActivity;

public class MainActivity extends AppCompatActivity {


    Button btnClientA;
    Button btnServerB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnClientA = findViewById(R.id.btn_client_A);
        btnServerB = findViewById(R.id.btn_server_B);

        btnClientA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ClientAActivity.class));
            }
        });


        btnServerB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ServerBActivity.class));

            }
        });

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
    }
}