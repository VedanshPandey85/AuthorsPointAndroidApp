package com.example.bookapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    //binding
    private ActivityPdfAddBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //arraylist to hold pdf categories
    private ArrayList<ModelCategory> categoryArrayList;

    //progressDialog

    private ProgressDialog progressDialog;

    //Tag for debugging
    private static final String Tag = "ADD_PDF_TAG";

    private  static final int PDF_PICK_CODE = 1000;

//    uri of picked pdf
    private Uri pdfUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_add);

        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);



//        init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        loadPdfCategories();


//        handle click,go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onBackPressed();
            }
        });


        // handle click ,attach pdf
        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                pdfPickIntent();

            }
        });
        //handle click ,pick category
        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                categoryPickDialog();
            }
        });

        //handle click ,upload pdf
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                        validateData();
            }
        });


    }

    private String title="",description="", category="";

    private void validateData() {
        //step 1:validate data
        Log.d(Tag,"validateData : validating data...");

        //get data
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        category = binding.categoryTv.getText().toString().trim();


        //validate data
        if(TextUtils.isEmpty(title)){

            Toast.makeText(this, "Enter title...", Toast.LENGTH_SHORT).show();


        }else if(TextUtils.isEmpty(description)){

            Toast.makeText(this, "Enter description...", Toast.LENGTH_SHORT).show();


        }else if(TextUtils.isEmpty(category)){

            Toast.makeText(this, "Pick category...", Toast.LENGTH_SHORT).show();

        }else if(pdfUri==null){
            Toast.makeText(this, "Pick Pdf...", Toast.LENGTH_SHORT).show();

        }else{

            //all data is valid pdf can be updated
            uploadPdfToStorage();
        }

    }

    private void uploadPdfToStorage() {

        //step 2: upload pdf to firebase storage

        Log.d(Tag,"uploadPdfToStorage: uploading to storage...");

        //show progress
        progressDialog.setMessage("Uploading pdf...");
        progressDialog.show();


        //timestamp
        long timestamp = System.currentTimeMillis();

        //path pdf in firebase storage
        String filePathAndName = "Books/"+timestamp;

        //storage reference


            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(Tag,"OnSucces : PDF uploaded to storage");
                        Log.d(Tag,"OnSucces : getting pdf url");


                        //get uri url
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String uploadedPdfUrl = ""+uriTask.getResult();
                        //upload to firebase db
                        uploadPdfInfoDb(uploadedPdfUrl,timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        Log.d(Tag,"onFailure : PDF upload failed due to "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "PDF failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();



                    }
                });

    }

    private void uploadPdfInfoDb(String uploadedPdfUrl, long timestamp) {
        //step 3: upload pdf to firebase db

        Log.d(Tag,"uploadPdfToStorage: uploading Pdf info to firebase db ...");
        progressDialog.setMessage("Uploading pdf info....");
        String uid = firebaseAuth.getUid();

        //setup data to uload
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timestamp);
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("category",""+category);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("timestamp",timestamp);


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        progressDialog.dismiss();
                        Log.d(Tag,"onSucess :Successfully uploaded...");
                        Toast.makeText(PdfAddActivity.this, "Successfully uploaded...", Toast.LENGTH_SHORT).show();


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        Log.d(Tag,"onFailure :Failed to upload due to "+e.getMessage());
                        Toast.makeText(PdfAddActivity.this, "Failed to upload due to "+e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });

    }

    private void loadPdfCategories() {

            Log.d(Tag,"loadPdfCategories Loading pdf categories...");
            categoryArrayList = new ArrayList<>();

        //db reference to load categories
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    categoryArrayList.clear();
                    for(DataSnapshot ds:snapshot.getChildren()){
                        ModelCategory model = ds.getValue(ModelCategory.class);
                        categoryArrayList.add(model);

                        Log.d(Tag,"onDataChange: "+model.getCategory());

                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });


    }

    private void categoryPickDialog() {

        Log.d(Tag,"categoryPickDialog: showing category pick dialog");

        //get string arrray of categories from arraylist

        String[] categoriesArray = new String[categoryArrayList.size()];
        for(int i=0;i<categoryArrayList.size();i++){

                categoriesArray[i]=categoryArrayList.get(i).getCategory();


        }
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //handle item click
                        //get clicked item from list
                        String category = categoriesArray[which];

                        //set category textview
                        binding.categoryTv.setText(category);
                        Log.d(Tag,"onclick:Selected Category : "+category);

                    }
                })
                .show();
    }
    private void pdfPickIntent() {

        Log.d(Tag,"pdfPickIntent: starting pdf pick intent ");
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Pdf"),PDF_PICK_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK){

            if(resultCode==PDF_PICK_CODE){

                Log.d(Tag,"onActivityResult:PDF Picked");
                pdfUri = data.getData();
                Log.d(Tag,"onActivityResult: URI: "+pdfUri);

            }else{
                Log.d(Tag,"onActivityResult: cancelled picking pdf");
                Toast.makeText(this , "cancelled picking pdf", Toast.LENGTH_SHORT).show();
            }

        }
    }
}