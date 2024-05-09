package com.example.hangman;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
    String word;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hangman); // hangman, not main (!)

        TextView guessed = findViewById(R.id.textView);
        winlose = findViewById(R.id.winlose);
        Button btnEnter = findViewById(R.id.btn);
        EditText inp = findViewById(R.id.input);
        ImageView hangman_image = findViewById(R.id.hangman);
        getWord();
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
                        word = jsonObject.get("response").toString();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            winlose.setText(word);
                            //winlose.setText("");  // To get rid of Загрузка...
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