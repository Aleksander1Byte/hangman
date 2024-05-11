package com.example.hangman;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnOnline;
    Button btnOffline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOnline = findViewById(R.id.start_online);
        btnOffline = findViewById(R.id.start_offline);

        btnOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GameActivity.class);
                view.getContext().startActivity(intent);}
        });

    }
}