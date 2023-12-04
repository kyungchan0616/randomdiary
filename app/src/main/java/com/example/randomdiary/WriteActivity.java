package com.example.randomdiary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class WriteActivity extends AppCompatActivity {

    private TextView dateView;
    private EditText editContent;
    private Button saveButton;
    private Button closeButton;
    private TextView todayTitle;
    private ImageView retitleImageView;
    private ImageView resetImageView; // Added resetImageView

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private String titleFromFirebase;
    private String themeFromFirebase;

    private FirebaseAuth firebaseAuth;

    private int clickCount; // 클릭 횟수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        dateView = findViewById(R.id.dateView);
        editContent = findViewById(R.id.edit_content);
        saveButton = findViewById(R.id.saveButton);
        closeButton = findViewById(R.id.closeButton);
        todayTitle = findViewById(R.id.today_title);
        retitleImageView = findViewById(R.id.retitleimageView);
        resetImageView = findViewById(R.id.resetimageView);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("todaytitle");

        firebaseAuth = FirebaseAuth.getInstance();

        // 이미 한 번 클릭한 경우를 체크
        checkAndResetClickCount();

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

        // 이미지 뷰 클릭 이벤트 처리
        retitleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭할 때마다 clickCount 증가
                clickCount++;

                // clickCount가 1인 경우에만 Todaytitle 갱신
                if (clickCount == 1) {
                    updateTodayTitle();
                    showToast("새로운 주제로 일기를 써볼까요?!");
                } else {
                    showToast("하루에 한 번만 가능해요 ㅠㅠ");
                }
            }
        });

        // 이미지 뷰 클릭 이벤트 처리 - resetImageView
        resetImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭할 때마다 clickCount 초기화
                clickCount = 0;
                showToast("ClickCount가 0으로 초기화되었습니다.");
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

    private void updateTodayTitle() {
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void checkAndResetClickCount() {
        // SharedPreferences에서 마지막으로 클릭한 날짜를 가져옴
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        long lastClickTimestamp = prefs.getLong("lastClickTimestamp", 0);

        // 현재 날짜와 비교
        Calendar cal = Calendar.getInstance();
        long currentTimestamp = cal.getTimeInMillis();

        // 하루가 지났으면 clickCount를 초기화하고 현재 날짜로 갱신
        if (currentTimestamp - lastClickTimestamp > 24 * 60 * 60 * 1000) {
            clickCount = 0;
            prefs.edit().putLong("lastClickTimestamp", currentTimestamp).apply();
        }
    }
}
