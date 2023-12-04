package com.example.randomdiary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ThemaActivity extends AppCompatActivity {

    private DatabaseReference diaryReference;
    private ArrayList<String> themaEntries;
    private ArrayAdapter<String> themaAdapter;

    private ArrayList<String> themaDiaryEntries;
    private ArrayAdapter<String> themaDiaryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thema);

        // Firebase에서 "diary" 데이터베이스 레퍼런스 가져오기
        diaryReference = FirebaseDatabase.getInstance().getReference("diary");

        // 현재 로그인한 사용자의 ID 가져오기
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getEmail() : "";

        // 화면 요소 초기화
        ListView themaListView = findViewById(R.id.ThemaView);

        themaEntries = new ArrayList<>();
        themaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, themaEntries);

        themaDiaryEntries = new ArrayList<>();
        themaDiaryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, themaDiaryEntries);

        themaListView.setAdapter(themaAdapter);

        // Firebase에서 데이터 가져와서 Thema 목록에 표시
        diaryReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                themaEntries.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String thema = snapshot.child("thema").getValue(String.class);
                    String userEmail = snapshot.child("user_email").getValue(String.class);

                    if (thema != null && currentUserId.equals(userEmail)) {
                        if (!themaEntries.contains(thema)) {
                            themaEntries.add(thema);
                        }
                    }
                }

                themaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });

        // Thema 목록에서 특정 Thema를 선택하면 해당 Thema에 속하는 일기 내용을 표시
        themaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedThema = themaEntries.get(position);
                showDiaryForThema(selectedThema);
            }
        });
    }

    private void showDiaryForThema(String selectedThema) {
        themaDiaryEntries.clear();

        diaryReference.orderByChild("thema").equalTo(selectedThema).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String entry = snapshot.child("content").getValue(String.class);

                    if (entry != null) {
                        themaDiaryEntries.add(entry);
                    }
                }

                themaDiaryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });
    }
}
