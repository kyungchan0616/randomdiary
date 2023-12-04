package com.example.randomdiary;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ThemeActivity extends AppCompatActivity {

    private DatabaseReference diaryReference;
    private ArrayList<String> theme1Entries;
    private ArrayList<String> theme2Entries;
    private ArrayList<String> theme3Entries;
    private ArrayAdapter<String> theme1Adapter;
    private ArrayAdapter<String> theme2Adapter;
    private ArrayAdapter<String> theme3Adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        // Firebase에서 "diary" 데이터베이스 레퍼런스 가져오기
        diaryReference = FirebaseDatabase.getInstance().getReference("diary");

        // 현재 로그인한 사용자의 ID 가져오기
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getEmail() : "";

        // 화면 요소 초기화
        ListView goodView = findViewById(R.id.theme1View);
        ListView sosoView = findViewById(R.id.theme2View);
        ListView badView = findViewById(R.id.theme3View);

        theme1Entries = new ArrayList<>();
        theme2Entries = new ArrayList<>();
        theme3Entries = new ArrayList<>();

        theme1Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, theme1Entries);
        theme2Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, theme2Entries);
        theme3Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, theme3Entries);

        goodView.setAdapter(theme1Adapter);
        sosoView.setAdapter(theme2Adapter);
        badView.setAdapter(theme3Adapter);

        // Firebase에서 데이터 가져와서 리스트에 표시
        diaryReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                theme1Entries.clear();
                theme2Entries.clear();
                theme3Entries.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String eval = snapshot.child("theme").getValue(String.class);
                    String userEmail = snapshot.child("user_email").getValue(String.class);

                    if (eval != null && currentUserId.equals(userEmail)) {
                        String entry = snapshot.child("content").getValue(String.class);
                        String date = snapshot.child("date").getValue(String.class);

                        if (entry != null && date != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yy년 MM월 dd일");
                            try {
                                Date parsedDate = sdf.parse(date);
                                String formattedDate = sdf.format(parsedDate);

                                String entryWithDate = formattedDate + " 의 일기\n" + entry;
                                switch (eval) {
                                    case "날씨":
                                        theme1Entries.add(entryWithDate);
                                        break;
                                    case "기분":
                                        theme2Entries.add(entryWithDate);
                                        break;
                                    case "음식":
                                        theme3Entries.add(entryWithDate);
                                        break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Update adapters and notify data set changes
                theme1Adapter.notifyDataSetChanged();
                theme2Adapter.notifyDataSetChanged();
                theme3Adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });
    }
}
