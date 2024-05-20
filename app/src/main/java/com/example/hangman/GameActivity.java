package com.example.hangman;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GameActivity extends AppCompatActivity {
    TextView winlose;
    TextView guessed;
    TextView guessedLettersView;
    Button btnEnter;
    EditText inp;
    ImageView hangman_image;
    StringBuilder toGuess = new StringBuilder();
    StringBuilder guessedLetters = new StringBuilder();
    String wordToGuess, playerInp;
    String alphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
    boolean campaign;
    int cnt = 1;
    int campaign_cnt = 1;

    Intent intent;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hangman);

        intent = getIntent();

        campaign = intent.getBooleanExtra("campaign", false);
        campaign_cnt = intent.getIntExtra("campaign_counter", 1);

        databaseHelper = new DatabaseHelper(this);

        guessed = findViewById(R.id.textView);
        guessedLettersView = findViewById(R.id.letters);
        winlose = findViewById(R.id.winlose);
        btnEnter = findViewById(R.id.btn);
        inp = findViewById(R.id.input);
        hangman_image = findViewById(R.id.hangman);
        hangman_image.setImageResource(getResources().getIdentifier("main_" + cnt, "drawable", getPackageName()));
        try {
            getWord();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void setup_rest() {
        for (int i = 0; i < wordToGuess.length(); i++) {
            toGuess.append("_ ");
        }
        guessed.setText(toGuess);
        char[] wordToGuessArray = wordToGuess.toCharArray();
        StringBuilder temp = new StringBuilder();
        for (char c : wordToGuessArray) {
            temp.append(c).append(" ");
        }
        wordToGuess = temp.toString();

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wrongGuess = true;
                playerInp = inp.getText().toString().toLowerCase();
                if (!playerInp.isEmpty()) {
                    inp.setText("");

                    for (int i = 0; i < wordToGuess.length(); i++) {

                        if (wordToGuess.charAt(i) == playerInp.charAt(0) && alphabet.contains(playerInp) && guessedLetters.indexOf(playerInp.toUpperCase()) == -1) {
                            toGuess.setCharAt(i, playerInp.charAt(0));
                            guessed.setText(toGuess);
                            wrongGuess = false;
                        }
                    }
                    if (alphabet.contains(playerInp) && guessedLetters.indexOf(playerInp.toUpperCase()) == -1) {
                        guessedLetters.append(playerInp.toUpperCase()).append(", ");
                        guessedLettersView.setText(guessedLetters);
                    }

                    if (wrongGuess) {
                        cnt += 1;
                        hangman_image.setImageResource(getResources().getIdentifier("main_" + cnt, "drawable", getPackageName()));
                        if (cnt == 7) {
                            winlose.setText("Проигрыш!");
                            if (!campaign)
                                guessedLettersView.setText(wordToGuess.toUpperCase());  // word reveal
                            btnEnter.setText("Назад");
                            btnEnter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            });
                        }
                    }

                    if (toGuess.indexOf("_") == -1) {
                        winlose.setText("Победа!");
                        btnEnter.setText("Назад");
                        btnEnter.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (campaign) {
                                    intent.putExtra("campaign_counter", campaign_cnt + 1);
                                }
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                    }
                }
                else {
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }

            }

        });
    }

    public void getWord() throws IOException {
        if (campaign) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT word FROM items WHERE id = ?", new String[] {String.valueOf(campaign_cnt + 1),});
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") String itemName = cursor.getString(cursor.getColumnIndex("word"));
                cursor.close();
                wordToGuess = itemName;
                setup_rest();
            }

            winlose.setText("");  // To get rid of Загрузка...
        } else {
            OkHttpClient client = new OkHttpClient();
            String url = "http://192.168.0.179:8000/one";
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            wordToGuess = jsonObject.get("response").toString();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        GameActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                winlose.setText("");  // To get rid of Загрузка...
                                setup_rest();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}