package ar.codeslu.plax.auth;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vanniktech.emoji.EmojiEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ar.codeslu.plax.MainActivity;
import ar.codeslu.plax.R;
import ar.codeslu.plax.global.Global;
import ar.codeslu.plax.lists.UserData;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class DataSet extends AppCompatActivity {

    //View
    EmojiEditText name, statue;
    CircleImageView avatar;
    Button next;
    //Vars
    String nameS, statueS, avaS;
    //Uri imgLocalpath;
    //Firebase
    FirebaseAuth mAuth;
    DatabaseReference mData;
    //compress
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_set);
        name = findViewById(R.id.nameE);
        statue = findViewById(R.id.statueE);
        avatar = findViewById(R.id.avatarSet);
        next = findViewById(R.id.nextS);
        //firebase init
        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance().getReference(Global.USERS);
        Global.currentactivity = this;

        mData.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserData userData = dataSnapshot.getValue(UserData.class);
                if (userData.getName() != null) {
                name.setText(userData.getName());
                statue.setText(userData.getStatue());
                avaS = userData.getAvatar();
                    if (avaS.equals("no")) {
                        Picasso.get()
                                .load(R.drawable.profile)
                                .error(R.drawable.errorimg)
                                .into(avatar);
                    } else {
                        Picasso.get()
                                .load(avaS)
                                .error(R.drawable.errorimg)
                                .into(avatar);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next.setEnabled(false);
                if (!TextUtils.isEmpty(name.getText().toString().trim())) {
                    nameS = name.getText().toString().trim();
                    statueS = statue.getText().toString();
                    if (avaS == null || TextUtils.isEmpty(avaS)) {
                        avaS = "no";
                    }

                    if (statueS == null || TextUtils.isEmpty(statueS))
                        statueS = Global.DEFAULT_STATUE;


                    if (avaS == null||!avaS.contains("file://")) {
                        statueS = statueS.trim();
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", nameS);
                        map.put("statue", statueS);
                        map.put("avatar", avaS);
                        map.put("id", mAuth.getCurrentUser().getUid());
                        mData.child(mAuth.getCurrentUser().getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(DataSet.this, MainActivity.class));
                                    finish();
                                    Toast.makeText(DataSet.this, R.string.signup_succ, Toast.LENGTH_SHORT).show();

                                } else
                                    Toast.makeText(DataSet.this, R.string.error, Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        //compress the photo
                        File newImageFile = new File(Uri.parse(avaS).getPath());
                        try {
                            compressedImageFile = new Compressor(DataSet.this)
                                    .setMaxHeight(500)
                                    .setMaxWidth(500)
                                    .setQuality(50)
                                    .compressToBitmap(newImageFile);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] thumbData = baos.toByteArray();
                        ////
                        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                        StorageReference riversRef = mStorageRef.child(Global.AvatarS + "/Ava_" + mAuth.getCurrentUser().getUid() + ".jpg");
                        riversRef.putBytes(thumbData)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("name", nameS);
                                        statueS = statueS.trim();
                                        map.put("statue", statueS);
                                        map.put("avatar", String.valueOf(downloadUrl));
                                        map.put("id", mAuth.getCurrentUser().getUid());
                                        mData.child(mAuth.getCurrentUser().getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    startActivity(new Intent(DataSet.this, MainActivity.class));
                                                    finish();
                                                    Toast.makeText(DataSet.this, R.string.signup_succ, Toast.LENGTH_SHORT).show();
                                                } else
                                                    Toast.makeText(DataSet.this, R.string.error, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                    }

                } else {
                    next.setEnabled(true);
                    Toast.makeText(DataSet.this, R.string.plz_name, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void changeprofile(View view) {
        if (ActivityCompat.checkSelfPermission(DataSet.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DataSet.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DataSet.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DataSet.this, new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    801);
        } else {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMinCropResultSize(400, 400)
                    .setAspectRatio(1, 1)
                    .start(DataSet.this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                avaS = String.valueOf(result.getUri());
                Picasso.get()
                        .load(avaS)
                        .error(R.drawable.errorimg)
                        .into(avatar);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Global.currentactivity = this;
    }


}

