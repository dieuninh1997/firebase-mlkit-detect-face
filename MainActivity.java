package com.example.sotatek.firebasemlkitdetectface;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Ninh";
    private static final int REQUEST_MULTIPLE = 1;

    private static final int PICK_FROM_CAMERA = 2;
    private static final int PICK_FROM_GALLARY = 3;


    private static String[] PERMISSION_ALL = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private Button btnOpenCamera;
    private Button btnChoosePhoto;
    private Button btnDetectFace;
    private ImageView imgPhoto;
    private RelativeLayout coordinatorLayout;
    private String cameraFilePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        requestMultiplePermissions();

        // High-accuracy landmark detection and face classification
        FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

        // Real-time contour detection of multiple faces
        FirebaseVisionFaceDetectorOptions realTimeOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .build();


        btnOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(MainActivity.this, "com.example.android.fileprovider", createImageFile()));
                    startActivityForResult(intent, PICK_FROM_CAMERA);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(galleryIntent, PICK_FROM_GALLARY);
            }
        });

        btnDetectFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            }
        });

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //This is the directory in which the file will be created. This is the default location of Camera photos
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for using again
        cameraFilePath = "file://" + image.getAbsolutePath();
        return image;
    }

    public int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath){
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

            Log.i("RotateImage", "Exif orientation: " + orientation);
            Log.i("RotateImage", "Rotate value: " + rotate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, requestCode + "  " + resultCode + " " + data);
        switch (requestCode) {
            case PICK_FROM_CAMERA:
                if (resultCode == RESULT_OK) {
                    imgPhoto.setImageURI(Uri.parse(cameraFilePath));
                    imgPhoto.setRotation(90);
                }
                break;
            case PICK_FROM_GALLARY:

                if (resultCode == RESULT_OK && data != null) {
                    Uri selectedPhoto = data.getData();
                    imgPhoto.setImageURI(selectedPhoto);
                    Log.d(TAG, ""+selectedPhoto);
                }
                break;
        }

    }

    private void initView() {
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        btnOpenCamera = findViewById(R.id.btnOpenCamera);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        btnDetectFace = findViewById(R.id.btnDetectFace);
        imgPhoto = findViewById(R.id.imgPhoto);
    }


    private void requestMultiplePermissions() {
        ArrayList<String> listPermissionsNeeded = new ArrayList<>();

        for (String permission : PERMISSION_ALL) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_MULTIPLE);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_MULTIPLE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(permissions[i])) {
                        new AlertDialog.Builder(this)
                                .setMessage("Your error message here")
                                .setPositiveButton("Allow", (dialog, which) -> requestMultiplePermissions())
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                .create()
                                .show();
                    }
                    return;
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
