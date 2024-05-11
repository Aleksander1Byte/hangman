package com.example.hangman;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Button btnOnline;
    Button btnOffline;
    String data1;
    int cnt;

    void update_counter(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        osw.write(cnt);
        osw.close();
        fos.close();
    }

    void get_content(File file) throws IOException {
            OkHttpClient client = new OkHttpClient();
            String url = "http://192.168.0.179:8000/" + 50;
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            data1 = jsonObject.toString();
                            FileOutputStream fos = new FileOutputStream(file);
                            OutputStreamWriter osw = new OutputStreamWriter(fos);
                            osw.write(data1);
                            osw.close();
                            fos.close();
                            System.out.println("File successfully written to external storage!");
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
            });
        }

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
                intent.putExtra("campaign", false);
                view.getContext().startActivity(intent);}
        });

        btnOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), GameActivity.class);
                intent.putExtra("campaign", true);
                view.getContext().startActivity(intent);}
        });

        try {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "hangman.txt");
                if (!file.exists()) {
                    file.createNewFile();
                    get_content(file);
                }
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "hangman_count.txt");
                if (!file.exists()) {
                    file.createNewFile();
                    update_counter(file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}