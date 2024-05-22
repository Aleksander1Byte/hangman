package com.example.hangman;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class GuessedWordsActivity extends AppCompatActivity {

    String[] get_words(File file) {
        String ret = "";

        try {
            FileInputStream fileInputStream = openFileInput(file.getName());
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            String lines;
            while ((lines = bufferedReader.readLine()) != null) {
                stringBuffer.append(lines.substring(0, 1).toUpperCase() + lines.substring(1) + " ");
            }
            ret = stringBuffer.toString();
            fileInputStream.close();
            inputStreamReader.close();
            bufferedReader.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return ret.split(" ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guessed_words);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(101);
                finish();
            }
        });

        File file = new File("/data/user/0/com.example.hangman/files/hangman_guessed.txt");
        String[] myListData = get_words(file);

        if (myListData[0] == "") myListData[0] = "Ещё не угадано ни одного слова";

        RecyclerView recyclerViewOKL = (RecyclerView) findViewById(R.id.recycleView);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(myListData);
        recyclerViewOKL.setHasFixedSize(true);
        recyclerViewOKL.setLayoutManager(new LinearLayoutManager(GuessedWordsActivity.this));
        recyclerViewOKL.setAdapter(adapter);

    }
}
