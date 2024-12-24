package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.example.bookapp.databinding.ActivityDashboardAdminBinding;
import com.example.bookapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardAdminActivity extends AppCompatActivity {


    private ActivityDashboardAdminBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ModelCategory> categoryArrayList;
    private AdapterCategory adapterCategory;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);

        binding = ActivityDashboardAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadCategories();



//        handle click,logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseAuth.signOut();
                checkUser();
            }
        });

        //handle pdf add btn
        binding.addPdfFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this,PdfAddActivity.class));

            }
        });


//        edit text change listner,search
        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                //called as and when user type each letter

                try {
                    adapterCategory.getFilter().filter(s);

                }catch (Exception e){


                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


//handle click ,start add category add screen
        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardAdminActivity.this,CategoryAddActivity.class));


            }
        });



    }

    private void loadCategories() {
//        init arayList
        categoryArrayList = new ArrayList<>();


//        get all categories from firebase > Categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                clear arrayList before adding data into it
                categoryArrayList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){

//                        get data
                    ModelCategory model = ds.getValue(ModelCategory.class);

//                    add to arrayList
                    categoryArrayList.add(model);
                }

//                setup adapter
                adapterCategory = new AdapterCategory(DashboardAdminActivity.this,categoryArrayList);

//                set adapter to recycler view

                binding.categoriesRv.setAdapter(adapterCategory);






            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUser() {

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser==null){
            startActivity(new Intent(this,MainActivity.class));
            finish();

        }else{

            String email = firebaseUser.getEmail();
            binding.subTitleTv.setText(email);

        }
    }
}