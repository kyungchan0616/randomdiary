package com.example.randomdiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class WriteActivity extends AppCompatActivity {

    private TextView dateView;
    private EditText editContent;
    private Button saveButton;
    private Button closeButton;
    private TextView todayTitle;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private String titleFromFirebase;
    private String themeFromFirebase;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        dateView = findViewById(R.id.dateView);
        editContent = findViewById(R.id.edit_content);
        saveButton = findViewById(R.id.saveButton);
        closeButton = findViewById(R.id.closeButton);
        todayTitle = findViewById(R.id.today_title);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("todaytitle");

        firebaseAuth = FirebaseAuth.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd");
        String currentDate = sdf.format(new Date());
        dateView.setText(currentDate);

        Intent intent = getIntent();
        if (intent.hasExtra("DIARY_KEY")) {
            String diaryKey = intent.getStringExtra("DIARY_KEY");
            loadDiaryForEdit(diaryKey);
        } else {
            loadRandomDiary();
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDiary();
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadDiaryForEdit(String diaryKey) {
        databaseReference.child(diaryKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String title = dataSnapshot.child("title").getValue(String.class);
                    String content = dataSnapshot.child("content").getValue(String.class);
                    String date = dataSnapshot.child("date").getValue(String.class);

                    todayTitle.setText(title);
                    editContent.setText(content);
                    dateView.setText(date);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 가져오기 실패 시 처리
            }
        });
    }

    private void loadRandomDiary() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                int size = (int) dataSnapshot.getChildrenCount();
                int randomIndex = new Random().nextInt(size);

                DataSnapshot selectedSnapshot = null;
                for (DataSnapshot snapshot : children) {
                    if (randomIndex-- == 0) {
                        selectedSnapshot = snapshot;
                        break;
                    }
                }

                if (selectedSnapshot != null) {
                    titleFromFirebase = String.valueOf(selectedSnapshot.child("title").getValue());
                    themeFromFirebase = String.valueOf(selectedSnapshot.child("thema").getValue());
                    todayTitle.setText(titleFromFirebase);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // 가져오기 실패 시 처리
            }
        });
    }

    private void saveDiary() {
        String userContent = editContent.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yy년 MM월 dd일");
        String currentDateAndTime = sdf.format(new Date());

        databaseReference = firebaseDatabase.getReference("diary");
        String uniqueKey = databaseReference.push().getKey();

        DatabaseReference saveReference = databaseReference.child(uniqueKey);

        // 사용자가 로그인되어 있으면 이메일 주소를 저장
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            saveReference.child("user_email").setValue(currentUser.getEmail());
        }

        saveReference.child("content").setValue(userContent);
        saveReference.child("date").setValue(currentDateAndTime);
        saveReference.child("theme").setValue(themeFromFirebase);
        saveReference.child("title").setValue(titleFromFirebase);

        // RadioGroup에서 선택된 RadioButton의 tag 값을 가져와 "eval"로 저장
        RadioGroup radioGroup = findViewById(R.id.moodRadioGroup);
        int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
            String eval = selectedRadioButton.getTag().toString();
            saveReference.child("eval").setValue(eval);
        }

        startHomeActivity();
    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
