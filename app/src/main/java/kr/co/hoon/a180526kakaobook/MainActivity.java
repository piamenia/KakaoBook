package kr.co.hoon.a180526kakaobook;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> data;
    ArrayAdapter<String> adapter;

    EditText keyword;

    Handler handler = new Handler(){
        String mes = "";
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 1:
                    mes = "다운로드 실패";
                    break;
                case 2:
                    mes = "파싱 실패";
                    break;
                case 3:
                    mes = "데이터 가져오기 성공";
                    adapter.notifyDataSetChanged();
                    break;
            }
            Toast.makeText(MainActivity.this, mes, Toast.LENGTH_LONG).show();
        }
    };

    class JSONThread extends Thread {
        @Override
        public void run() {
            String json = "";
            try{
                // 다운로드 받을 주소
                String addr = "http://apis.daum.net/search/book?output=json&q=";
                addr += URLEncoder.encode(keyword.getText().toString(), "UTF-8");
                // URL 생성하고 연결
                URL url = new URL(addr);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                // Kakao 인증
                conn.setRequestProperty("Authorization", "KakaoAK 4ffe221d23e1d16cee397dc4f1539f10");
                // 옵션
                conn.setUseCaches(false);
                conn.setConnectTimeout(30000);

                // 문자열 읽기 스트림
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                while(true){
                    String line = br.readLine();
                    if(line==null) break;
                    sb.append(line);
                }
                json = sb.toString();
                // JSON데이터를 확인하기 위해 출려해보기
//                Log.e("json", json);
                br.close();
                conn.disconnect();
            }catch (Exception e){
                Log.e("다운로드 실패", e.getMessage());
                handler.sendEmptyMessage(1);
            }

            try{
                JSONObject j = new JSONObject(json);
                JSONObject channel = j.getJSONObject("channel");
                JSONArray item = channel.getJSONArray("item");
                data.clear();
                for(int i = 0;i<item.length();i=i+1){
                    JSONObject temp = item.getJSONObject(i);
                    String title = temp.getString("title");
                    String description = temp.getString("description");
                    data.add(title + " : " + description);
                }
                handler.sendEmptyMessage(3);

            }catch(Exception e){
                Log.e("파싱실패",e.getMessage());
                handler.sendEmptyMessage(2);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        keyword = (EditText)findViewById(R.id.keyword);
        listView = (ListView)findViewById(R.id.listView);

        // 리스트뷰에 data 출력
        data = new ArrayList<>();
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);
        listView.setDivider(new ColorDrawable(Color.parseColor("#555555")));
        listView.setDividerHeight(3);

        Button search = (Button)findViewById(R.id.search);
        search.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                JSONThread th = new JSONThread();
                th.start();
            }
        });
    }
}
