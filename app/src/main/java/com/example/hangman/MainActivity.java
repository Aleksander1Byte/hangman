package com.example.hangman;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hangman); // hangman, not main (!)

        TextView guessed = findViewById(R.id.textView);
        TextView winlose = findViewById(R.id.winlose);
        winlose.setText("");  // To get rid of Загрузка...
        Button btnEnter = findViewById(R.id.btn);
        EditText inp = findViewById(R.id.input);
        ImageView hangman_image = findViewById(R.id.hangman);
    }
}