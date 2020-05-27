package jp.ac.titech.itpro.sdl.camera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static int REQ_PHOTO = 1234;
    private final static int REQ_PHOTO2 = 1;
    private Bitmap photoImage = null;
    String currentPhotoPath;
    private SharedPreferences dataStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //機種によってカメラの挙動が異なったので、解像度の高い方のチャレンジは、SharedPreferencesを使用
        dataStore = getSharedPreferences("Data", MODE_PRIVATE);

        Button photoButton = findViewById(R.id.photo_button);
        Button photoButton2 = findViewById(R.id.photo_button2);


        //通常の課題
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // TODO: You should setup appropriate parameters for the intent ->OK

                PackageManager manager = getPackageManager();
                List activities = manager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (!activities.isEmpty()) {
                    startActivityForResult(intent, REQ_PHOTO);
                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_no_activities, Toast.LENGTH_LONG).show();
                }
            }
        });

        //解像度の高い方のチャレンジ
        photoButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        Toast.makeText(MainActivity.this, R.string.toast_no_activities, Toast.LENGTH_LONG).show();

                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                                "com.camera.android.fileprovider",
                                photoFile);

                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQ_PHOTO2);

                        String st_photoURI= photoURI.toString();

                        SharedPreferences.Editor editor = dataStore.edit();
                        editor.putString("DataString", st_photoURI);
                        editor.apply();
                    }else {
                        Toast.makeText(MainActivity.this, R.string.toast_no_activities, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    private void showPhoto() {
        if (photoImage == null) {
            return;
        }
        ImageView photoView = findViewById(R.id.photo_view);
        photoView.setImageBitmap(photoImage);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if (reqCode == REQ_PHOTO) {
            if (resCode == RESULT_OK) {
                // TODO: You should implement the code that retrieve a bitmap image ->OK
                Bundle extras = data.getExtras();
                photoImage = (Bitmap) extras.get("data");
            }
        }

        if (reqCode == REQ_PHOTO2) {
            if (resCode == RESULT_OK) {
                String dataString = dataStore.getString("DataString", null);
                Uri dataString_Uri= Uri.parse(dataString);
                try {
                    photoImage = MediaStore.Images.Media.getBitmap(getContentResolver(), dataString_Uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showPhoto();
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}