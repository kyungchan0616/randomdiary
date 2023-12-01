package com.example.randomdiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private String themaFromFirebase;

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
                    themaFromFirebase = String.valueOf(selectedSnapshot.child("thema").getValue());
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
        saveReference.child("content").setValue(userContent);
        saveReference.child("date").setValue(currentDateAndTime);
        saveReference.child("thema").setValue(themaFromFirebase);
        saveReference.child("title").setValue(titleFromFirebase);

        startHomeActivity();
    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
