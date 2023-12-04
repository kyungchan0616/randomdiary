package com.example.randomdiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditActivity extends AppCompatActivity {

    private DatabaseReference diaryReference;
    private EditText editContent;
    private TextView editDate;
    private TextView editTitle;
    private String diaryKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Firebase에서 "diary" 데이터베이스 레퍼런스 가져오기
        diaryReference = FirebaseDatabase.getInstance().getReference("diary");

        // XML에서 정의한 뷰들과 연결
        editContent = findViewById(R.id.edit_content);
        editDate = findViewById(R.id.edit_date);
        editTitle = findViewById(R.id.edit_title);

        // Intent로 전달된 다이어리 키 가져오기
        diaryKey = getIntent().getStringExtra("DIARY_KEY");

        // 다이어리 정보를 데이터베이스에서 가져와 UI에 설정하는 메서드 호출
        loadDiaryData();

        // 저장하기 버튼을 클릭하면 데이터베이스를 업데이트하고 HomeActivity로 이동하는 메서드 호출
        findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateContentAndNavigateToHome();
            }
        });

        // 삭제 버튼을 클릭하면 다이어리를 삭제하고 HomeActivity로 이동하는 메서드 호출
        findViewById(R.id.deleteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDiaryAndNavigateToHome();
            }
        });

        // Close 버튼을 클릭하면 현재 화면을 닫고 이전 화면으로 돌아가거나 HomeActivity로 이동
        findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 화면을 닫고 이전 화면으로 돌아가거나 HomeActivity로 이동
                closeActivity();
            }
        });
    }

    private void loadDiaryData() {
        // 선택한 다이어리의 키를 사용하여 데이터베이스에서 해당 다이어리 정보 가져오기
        diaryReference.child(diaryKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // dataSnapshot을 사용하여 다이어리 정보를 가져와 UI에 설정
                String content = dataSnapshot.child("content").getValue(String.class);
                String date = dataSnapshot.child("date").getValue(String.class);
                String title = dataSnapshot.child("title").getValue(String.class);

                // 가져온 정보를 UI에 설정
                if (content != null) {
                    editContent.setText(content);
                } else {
                    editContent.setText("No content available");
                }

                if (date != null) {
                    editDate.setText(date);
                } else {
                    editDate.setText("No date available");
                }

                if (title != null) {
                    editTitle.setText(title);
                } else {
                    editTitle.setText("No title available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
            }
        });
    }

    private void updateContentAndNavigateToHome() {
        // 사용자가 수정한 content 내용 가져오기
        String updatedContent = editContent.getText().toString();

        // 데이터베이스에서 해당 다이어리의 참조 가져오기
        DatabaseReference diaryToUpdate = diaryReference.child(diaryKey);

        // content를 데이터베이스에 업데이트
        diaryToUpdate.child("content").setValue(updatedContent);

        // 업데이트가 성공했다면 알림 등 추가 가능
        Toast.makeText(this, "다이어리 내용이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();

        // HomeActivity로 이동
        startActivity(new Intent(this, HomeActivity.class));
        finish(); // 현재 EditActivity 종료
    }

    private void deleteDiaryAndNavigateToHome() {
        // 데이터베이스에서 해당 다이어리의 참조 가져오기
        DatabaseReference diaryToDelete = diaryReference.child(diaryKey);

        // 다이어리를 삭제
        diaryToDelete.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // 삭제가 성공했다면 알림 등 추가 가능
                    Toast.makeText(EditActivity.this, "다이어리가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    // 삭제가 실패했을 경우 알림 등 추가 가능
                    Toast.makeText(EditActivity.this, "다이어리 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }

                // HomeActivity로 이동
                startActivity(new Intent(EditActivity.this, HomeActivity.class));
                finish(); // 현재 EditActivity 종료
            }
        });
    }

    // 현재 화면을 닫고 이전 화면으로 돌아가거나 HomeActivity로 이동
    private void closeActivity() {
        finish(); // 현재 EditActivity 종료
    }
}
