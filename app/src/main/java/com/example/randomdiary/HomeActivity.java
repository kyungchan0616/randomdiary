package com.example.randomdiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference diaryReference;
    private ArrayList<String> diaryEntries;
    private ArrayList<String> diaryKeys;
    private ArrayAdapter<String> adapter;

    private Button writeButton;
    private Button evalButton;
    private Button themaButton;

    // Firebase Authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Firebase에서 "diary" 데이터베이스 레퍼런스 가져오기
        diaryReference = FirebaseDatabase.getInstance().getReference("diary");

        // Firebase Authentication 초기화
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // 화면 요소 초기화
        ListView diaryListView = findViewById(R.id.diaryListView);
        diaryKeys = new ArrayList<>();
        diaryEntries = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, diaryEntries);
        diaryListView.setAdapter(adapter);

        // Firebase에서 데이터 가져와서 리스트에 표시
        diaryReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                diaryKeys.clear();
                diaryEntries.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String diaryKey = snapshot.getKey(); // 데이터베이스에서의 고유한 키 가져오기
                    String diaryContent = snapshot.child("content").getValue(String.class);
                    String diaryDate = snapshot.child("date").getValue(String.class);
                    String userId = snapshot.child("user_email").getValue(String.class); // 사용자 ID 가져오기

                    // 현재 로그인한 사용자의 일기만 표시
                    if (currentUser != null && currentUser.getEmail().equals(userId)) {
                        // 리스트에 표시할 문자열 생성
                        String entry = diaryDate + "의 일기 ";

                        // 리스트에 추가
                        diaryKeys.add(diaryKey);
                        diaryEntries.add(entry);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });

        // 리스트 아이템 클릭 시 수정 화면으로 이동
        diaryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDiaryKey = diaryKeys.get(position);
                startEditActivity(selectedDiaryKey);
            }
        });

        // "글쓰기" 버튼을 레이아웃에서 찾음
        writeButton = findViewById(R.id.writeButton);
        evalButton = findViewById(R.id.evalButton);
        themaButton = findViewById(R.id.themaButton);

        // 버튼에 클릭 리스너 설정
        writeButton.setOnClickListener(this);
        evalButton.setOnClickListener(this);
        themaButton.setOnClickListener(this);
    }

    private void startEditActivity(String selectedDiaryKey) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("DIARY_KEY", selectedDiaryKey);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        // 각 버튼에 대한 클릭 이벤트 처리
        switch (v.getId()) {
            case R.id.writeButton:
                checkIfTodayDiaryExists();
                break;
            case R.id.evalButton:
                startEvalActivity();
                break;
            case R.id.themaButton:
                startThemaActivity();
                break;
        }
    }

    private void startEvalActivity() {
        Intent intent = new Intent(this, EvalActivity.class);
        startActivity(intent);
    }

    private void startThemaActivity() {
        Intent intent = new Intent(this, ThemaActivity.class);
        startActivity(intent);
    }

    private void checkIfTodayDiaryExists() {
        SimpleDateFormat sdf = new SimpleDateFormat("yy년 MM월 dd일");
        String currentDate = sdf.format(new Date());

        // 로그인한 사용자의 ID 가져오기
        String currentUserEmail = (currentUser != null) ? currentUser.getEmail() : "";

        diaryReference.orderByChild("date").equalTo(currentDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // 각 일기의 작성자 ID 가져오기
                    String userId = snapshot.child("user_email").getValue(String.class);

                    // 로그인한 사용자의 ID와 일기 작성자의 ID 비교
                    if (currentUserEmail.equals(userId)) {
                        // 오늘 날짜에 해당하는 일기가 이미 작성되었음
                        // 여기에서 사용자에게 메시지를 보여주거나 다른 동작을 수행할 수 있음
                        showToast("오늘은 이미 일기를 썼네요");
                        return; // 일기가 이미 작성되었다면 더 이상 검사할 필요가 없으므로 종료
                    }
                }

                // 오늘 날짜에 해당하는 일기가 아직 작성되지 않았음
                Intent intent = new Intent(HomeActivity.this, WriteActivity.class);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
