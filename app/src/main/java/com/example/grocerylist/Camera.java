package com.example.grocerylist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Camera {
    private Activity activity;
    private String image_path;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int RESULT_LOAD_IMAGE = 1;

    public Camera(Activity _activity) {
        this.activity = _activity;
        this.image_path = "";
    }

    public String dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.activity.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = savePhoto();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this.activity,
                        "com.example.grocerylist.fileprovider",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                this.activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
        return this.image_path;
    }

    private File savePhoto() throws IOException {
        String currentPhotoPath;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName + ".jpeg");
        currentPhotoPath = image.getAbsolutePath();
        this.image_path = currentPhotoPath;
        return image;
    }

    public void setPicture(String imagePath) {
        ImageView imageView = this.activity.findViewById(R.id.image);
        int targetW = 250;
        int targetH = 250;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        if (bitmap != null) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(bitmap);
        }
    }
}