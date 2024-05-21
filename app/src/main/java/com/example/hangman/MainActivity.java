package com.example.hangman;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    Button btnOnline;
    Button btnOffline;
    String data1;
    int cnt = 1;

    void update_counter(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        osw.write(Integer.toString(cnt));
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
                        setup_db(file);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                setResult(3);
                finish();
            }
        });
    }

    void setup_db(File file) {
        ArrayList<Word> itemList = new ArrayList<>();

        try {
            FileInputStream fis = openFileInput(file.getName());
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            int id = 1;
            while ((line = br.readLine()) != null) {
                JSONObject jsonObject = new JSONObject(line);
                data1 = jsonObject.get("response").toString();
                String[] parts = data1.split(",");
                for (int i = 0; i < parts.length; i++) {
                    if (i == 0) parts[i] = parts[i].substring(2, parts[i].length() - 1);
                    else if (i == parts.length - 1) parts[i] = parts[i].substring(1, parts[i].length() - 2);
                    else parts[i] = parts[i].substring(1, parts[i].length() - 1);
                    Word item = new Word(id, parts[i]);
                    itemList.add(item);
                    id++;
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        for (Word item : itemList) {
            ContentValues values = new ContentValues();
            values.put("word", item.getName());
            db.insert("items", null, values);
        }
        db.close();
        System.out.println("Database was set up correctly");
    }
    int get_cnt(File file) {
        String ret = "";

        try {
            FileInputStream fileInputStream = openFileInput(file.getName());
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer stringBuffer = new StringBuffer();
            String lines;
            while ((lines = bufferedReader.readLine()) != null) {
                stringBuffer.append(lines);
            }
                ret = stringBuffer.toString();
            fileInputStream.close();
            inputStreamReader.close();
            bufferedReader.close();
            } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return Integer.parseInt(ret);
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
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("campaign", false);
                intent.putExtra("campaign_counter", 1);
                MainActivity.this.startActivityForResult(intent, 11);
            }
        });

        btnOffline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GameActivity.class);
                intent.putExtra("campaign", true);
                intent.putExtra("campaign_counter", cnt);
                MainActivity.this.startActivityForResult(intent, 11);
            }
        });

        try {
            File file = new File("/data/user/0/com.example.hangman/files/hangman.txt");
            if (!file.exists()) {
                file.createNewFile();
                get_content(file);
            }
            file = new File("/data/user/0/com.example.hangman/files/hangman_count.txt");
            if (!file.exists()) {
                file.createNewFile();
                update_counter(file);
            }
            else {
                cnt = get_cnt(file);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data.getBooleanExtra("campaign", false)) {
                cnt = data.getIntExtra("campaign_counter", 1);
                File file = new File("/data/user/0/com.example.hangman/files/hangman_count.txt");
                if (file.exists()) {
                    try {
                        update_counter(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (resultCode == 3) {
            Toast toast = Toast.makeText(getApplicationContext(), "Не удаётся получить ответ от сервера", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}