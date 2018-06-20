package me.dats.com.datsme.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import me.dats.com.datsme.LoginActivity;
import me.dats.com.datsme.R;

public class ProfileActivity extends AppCompatActivity {

    @BindView(R.id.user_image)
    CircleImageView userimamge;
    @BindView(R.id.user_displayname)
    EditText user_displayname;
    private DatabaseReference mDatabase;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageRef;
    private ProgressDialog mProgessDialog;
    String thumb_downloadurl = null;
    String download_url = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mDatabase.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        userimamge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setAspectRatio(1, 1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropWindowSize(500, 500)
                        .start(ProfileActivity.this);

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            final CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mProgessDialog = new ProgressDialog(this);
                mProgessDialog.setMessage("Please wait while we upload the image");
                mProgessDialog.setTitle("Uploading Image...");
                mProgessDialog.setCanceledOnTouchOutside(true);
                mProgessDialog.show();


                Uri resultUri = result.getUri();

                File thumb_file = new File(resultUri.getPath());

                final String current_userid = mCurrentUser.getUid();

                Bitmap thumb_bitmap = null;
                final byte[] thumb_byte;

                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                thumb_byte = baos.toByteArray();

                StorageReference filepath = mStorageRef.child("profile_images").child(current_userid + ".jpg");
                final StorageReference thumb_filepath = mStorageRef.child("profile_images").child("thumbs").child(current_userid + ".jpg");


                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            download_url = task.getResult().toString();
                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    if (thumb_task.isSuccessful()) {

                                        thumb_downloadurl = thumb_task.getResult().toString();

                                        mProgessDialog.dismiss();
                                        userimamge.setImageURI(result.getUri());

                                    } else {
                                        mProgessDialog.dismiss();
                                        Toast.makeText(ProfileActivity.this, "Try Again Later", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });


                        } else {
                            mProgessDialog.dismiss();
                            Toast.makeText(ProfileActivity.this, "Try Again Later", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void Savetodatabase(View view) {

        if (user_displayname.getText() != null) {
            Map<String, String> userMap = new HashMap<>();
            userMap.put("name", user_displayname.getText().toString());
            userMap.put("email", mCurrentUser.getEmail());
            userMap.put("image", download_url);
            userMap.put("thumb_image", thumb_downloadurl);

            mDatabase.setValue(userMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mProgessDialog.dismiss();
                                startActivity(new Intent(ProfileActivity.this, MapsActivity.class));
                                finish();
                            }
                        }
                    });

        } else {
            user_displayname.setError("Cannot Be Empty");
        }
    }
}