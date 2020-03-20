package com.stego.stegoscanner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

public class CameraAppActivity extends AppCompatActivity{


    private static final int VIDEO_REQUEST = 101;
    private Uri videoUri = null;
    private ImageView imageView;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_app);
        imageView =findViewById(R.id.imageViewThree);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void captureVideo(View view) {
         Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

         if(videoIntent.resolveActivity(getPackageManager())!=null){
                startActivityForResult(videoIntent,VIDEO_REQUEST);
         }
    }


    public void playVideo(View view) {
        Intent playIntent =new Intent(this,VideoPlayActivity.class);
        if(videoUri !=null) {
            playIntent.putExtra("videoUrl", videoUri.toString());
            startActivity(playIntent);
        }else{
            Toast.makeText(getApplicationContext(), "No Available Advertisment" , Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==VIDEO_REQUEST && resultCode==RESULT_OK){
            videoUri =data.getData();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
