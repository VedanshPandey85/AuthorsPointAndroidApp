package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {


    private ActivityRegisterBinding binding;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        firebase init
        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    onBackPressed();
            }
        });



        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    validateData();

            }
        });



    }
    private String name ="",email="",password="";

    private void validateData() {

        name = binding.nameEdt.getText().toString().trim();
        email = binding.emailEdt.getText().toString().trim();
        password = binding.passwordEdt.getText().toString().trim();
        String cPassword = binding.cPasswordEdt.getText().toString().trim();


        if(TextUtils.isEmpty(name)){
                Toast.makeText(this,"Enter you name",Toast.LENGTH_SHORT).show();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

                Toast.makeText(this,"Invalid email pattern",Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(password)){

                Toast.makeText(this,"Confirm Password...!",Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(cPassword)){
                Toast.makeText(this,"Confirm password..!",Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(cPassword)){

                Toast.makeText(this,"Password dosen't match..!",Toast.LENGTH_SHORT).show();
       }
        else{
                createUserAccount();
        }
    }

    private void createUserAccount() {

//            show progress

        progressDialog.setMessage("Creating Account");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        progressDialog.dismiss();
                        //  Account creation succes
                            updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });


    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving user info....");
        long timestamp = System.currentTimeMillis();

        String uid = firebaseAuth.getUid();

        HashMap<String,Object>hashMap = new HashMap<>();
        hashMap.put("uid",uid);
        hashMap.put("email",email);
        hashMap.put("name",name);
        hashMap.put("profileimage","");
        hashMap.put("userType","user");
        hashMap.put("timestamp",timestamp);


       DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(RegisterActivity.this, "", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this,DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT);
                    }
                });
    }
}
