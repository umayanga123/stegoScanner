package com.stego.stegoscanner;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST;

public class VideoPlayActivity extends AppCompatActivity {

    private VideoView mVideoView;
    private BarcodeDetector barcodeDetector;
    private String TAG ="Save Image";
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        mVideoView =findViewById(R.id.videoView);

        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        Uri videoUri = Uri.parse(getIntent().getExtras().getString("videoUrl"));


        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(getApplicationContext(),videoUri);
        final Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(500,OPTION_CLOSEST); //unit in microsecond


        mVideoView.setVideoURI(videoUri);
        mVideoView.start();
        detectBarCode(bmFrame);
    }


    private void detectBarCode(Bitmap bmFrame) {
        if (barcodeDetector.isOperational() && bmFrame != null) {

            File pictureFile  = getOutputMediaFile();
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                bmFrame.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();
                //
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                imageUri = FileProvider.getUriForFile(VideoPlayActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider", pictureFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);


            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }




            Bitmap bitmap = null;
            try {
                bitmap = decodeBitmapUri(this,imageUri );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();

            SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);
            for (int index = 0; index < barcodes.size(); index++) {
                Barcode code = barcodes.valueAt(index);
                Toast.makeText(getApplicationContext(), code.displayValue , Toast.LENGTH_LONG).show();
            }
            if (barcodes.size() == 0) {
                Toast.makeText(getApplicationContext(), "No barcode could be detected. Please try again." , Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Detector initialisation failed" , Toast.LENGTH_SHORT).show();
        }
    }

    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(Environment.getExternalStorageDirectory()+ File.separator + mImageName);
        return mediaFile;


    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }

}
