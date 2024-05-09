package com.example.hangman;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
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
    int cnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hangman); // hangman, not main (!)

        guessed = findViewById(R.id.textView);
        guessedLettersView = findViewById(R.id.letters);
        winlose = findViewById(R.id.winlose);
        btnEnter = findViewById(R.id.btn);
        inp = findViewById(R.id.input);
        hangman_image = findViewById(R.id.hangman);
        getWord();
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
                    }
                }

                if (toGuess.indexOf("_") == -1) {
                    winlose.setText("Победа!");
                }

            }

        });
    }

    public void getWord() {
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
                    MainActivity.this.runOnUiThread(new Runnable() {
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