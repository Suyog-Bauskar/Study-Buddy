package com.example.studdybuddyadmin;

import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    private Button uploadBtn;
    private String branch, semester, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadBtn = findViewById(R.id.upload);
        uploadBtn.setOnClickListener(v -> selectBranchAndSemester());

        if (Build.VERSION.SDK_INT < 33) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private void selectBranchAndSemester() {
        AlertDialog.Builder branchDialog = new AlertDialog.Builder(MainActivity.this);
        branchDialog.setTitle("Branch");
        String[] items = {"Mechanical", "Civil", "Computer", "Electrical"};
        branchDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
            branch = items[which];
            dialog.dismiss();
            AlertDialog.Builder semesterDialog = new AlertDialog.Builder(MainActivity.this);
            semesterDialog.setTitle("Semester");
            String[] items2 = {"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6"};
            semesterDialog.setSingleChoiceItems(items2, -1, (dialog1, which1) -> {
                dialog1.dismiss();
                which1++;
                semester = which1 + "";
                AlertDialog.Builder typeDialog = new AlertDialog.Builder(MainActivity.this);
                typeDialog.setTitle("Type");
                String[] items3 = {"Books", "Notes", "Manual"};
                typeDialog.setSingleChoiceItems(items3, -1, (dialog2, which2) -> {
                    dialog2.dismiss();
                    type = items3[which2];
                    selectPDF();
                });
                typeDialog.create().show();
            });
            semesterDialog.create().show();
        });
        branchDialog.create().show();
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                if (data != null) {
                    Uri uri = data.getData();
                    Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    String fileName = returnCursor.getString(nameIndex);
                    FirebaseStorage.getInstance().getReference().child(branch + "/" + semester + "/" + type + "/" + fileName)
                            .putFile(uri)
                            .addOnSuccessListener(taskSnapshot -> Toast.makeText(getApplicationContext(), "File uploaded successfully", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });

    private void selectPDF() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        resultLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        } else {
            Toast.makeText(getApplicationContext(), "Permission is Denied", Toast.LENGTH_SHORT).show();
        }
    }
}