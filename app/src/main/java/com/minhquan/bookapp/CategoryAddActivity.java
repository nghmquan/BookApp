package com.minhquan.bookapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.minhquan.bookapp.databinding.ActivityCategoryAddBinding;

import java.util.HashMap;

public class CategoryAddActivity extends AppCompatActivity {

    //view binding
    private ActivityCategoryAddBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //configure progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Vui lòng đợi!");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //handle click, begin upload category
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private String category = "";
    private void validateData() {
        //get data
        category = binding.categoryEt.getText().toString().trim();
        //validate if not empty
        if(TextUtils.isEmpty(category)){
            Toast.makeText(this,"Vui lòng nhập thể loại sách!", Toast.LENGTH_SHORT).show();
        }else{
            checkCategoryExists(category);
        }
    }

    //check category exists
    private void checkCategoryExists(String category){
        //convert category to lowercase for comparison
        final String categoryLowercase = category.toLowerCase();

        //show progress
        progressDialog.setMessage("Đang kiểm tra thể loại sách!");
        progressDialog.show();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean categoryExists = false;
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    String existingCategory = snapshot.child("category").getValue(String.class);
                    if(existingCategory != null && existingCategory.toLowerCase().equals(categoryLowercase)){
                        //category already exists
                        categoryExists = true;
                        break;
                    }
                }

                if(categoryExists){
                    progressDialog.dismiss();
                    Toast.makeText(CategoryAddActivity.this,"Thể loại sách đã tồn tại!", Toast.LENGTH_SHORT).show();
                }
                else{
                    //category does not exist, proceed with adding
                    addCategoryFirebase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //add category into firebase
    private void addCategoryFirebase() {
        //show progress
        progressDialog.setMessage("Đang thêm thể loại sách!");
        progressDialog.show();

        //get timestamp
        long timestamp = System.currentTimeMillis();

        //setup info to add in firebase db
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", ""+timestamp);
        hashMap.put("category", ""+category);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", ""+firebaseAuth.getUid());

        //add to firebase ... Database Root > Categories > categoryId > category info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //category add success
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this, "Thêm thể loại thành công!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //category add failed
                        progressDialog.dismiss();
                        Toast.makeText(CategoryAddActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
}