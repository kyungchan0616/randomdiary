package com.example.randomdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

public class EvalActivity extends AppCompatActivity {

    private DatabaseReference diaryReference;
    private ArrayList<String> goodEntries;
    private ArrayList<String> sosoEntries;
    private ArrayList<String> badEntries;
    private ArrayAdapter<String> goodAdapter;
    private ArrayAdapter<String> sosoAdapter;
    private ArrayAdapter<String> badAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eval);

        // Firebase에서 "diary" 데이터베이스 레퍼런스 가져오기
        diaryReference = FirebaseDatabase.getInstance().getReference("diary");

        // 현재 로그인한 사용자의 ID 가져오기
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getEmail() : "";

        // 화면 요소 초기화
        ListView goodView = findViewById(R.id.goodView);
        ListView sosoView = findViewById(R.id.sosoView);
        ListView badView = findViewById(R.id.badView);

        goodEntries = new ArrayList<>();
        sosoEntries = new ArrayList<>();
        badEntries = new ArrayList<>();

        goodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, goodEntries);
        sosoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sosoEntries);
        badAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, badEntries);

        goodView.setAdapter(goodAdapter);
        sosoView.setAdapter(sosoAdapter);
        badView.setAdapter(badAdapter);

        // Firebase에서 데이터 가져와서 리스트에 표시
        diaryReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goodEntries.clear();
                sosoEntries.clear();
                badEntries.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String eval = snapshot.child("eval").getValue(String.class);
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
                                    case "good":
                                        goodEntries.add(entryWithDate);
                                        break;
                                    case "soso":
                                        sosoEntries.add(entryWithDate);
                                        break;
                                    case "bad":
                                        badEntries.add(entryWithDate);
                                        break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Update adapters and notify data set changes
                goodAdapter.notifyDataSetChanged();
                sosoAdapter.notifyDataSetChanged();
                badAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });
    }
}
