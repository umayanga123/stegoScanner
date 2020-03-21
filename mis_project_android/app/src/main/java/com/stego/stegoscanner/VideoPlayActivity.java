package com.stego.stegoscanner;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST;

public class VideoPlayActivity extends AppCompatActivity {

    private VideoView mVideoView;
    private BarcodeDetector barcodeDetector;
    private String TAG = "Save Image";
    private Uri imageUri;
    private TextView txtResultBody2;
    private Boolean status = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        mVideoView = findViewById(R.id.videoView);
        txtResultBody2 = findViewById(R.id.txtResultsBody2);

        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        Uri videoUri = Uri.parse(getIntent().getExtras().getString("videoUrl"));


        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(getApplicationContext(), videoUri);

        mVideoView.setVideoURI(videoUri);
        mVideoView.start();
        txtResultBody2.setText("Processing ...");

        new Thread(new Runnable() {
            public MediaMetadataRetriever mediaMetadataRetriever;
            public Runnable init(MediaMetadataRetriever mediaMetadataRetriever) {
                this.mediaMetadataRetriever = mediaMetadataRetriever;
                return this;
            }

            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    final Bitmap bmFrame = mediaMetadataRetriever.getFrameAtTime(i, OPTION_CLOSEST); //unit in microsecond
                    detectBarCode(bmFrame, i);
                    if (status) {
                        break;
                    }
                }
            }
        }.init(mediaMetadataRetriever)).start();

    }


    private void detectBarCode(final Bitmap bmFrame, int i) {
        if (barcodeDetector.isOperational() && bmFrame != null) {

            File pictureFile = getOutputMediaFile(i);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
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


            try {
                Bitmap bitmap = decodeBitmapUri(this, imageUri,bmFrame);

                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                // create output bitmap
                final Bitmap bmOut = Bitmap.createBitmap(width, height, bitmap.getConfig());
                // color information
                int B;
                int pixel;

                // scan through all pixels
                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        // get pixel color
                        pixel = bitmap.getPixel(x, y);
                        B = Color.blue(pixel);
                        int gray = (int) (B);

                        // use 128 as threshold, above -> white, below -> black
                        if (gray > 128) {
                            bmOut.setPixel(x, y, Color.argb(255, 255, 255, 255));
                        } else {
                            bmOut.setPixel(x, y, Color.argb(255, 0, 0, 0));

                        }
                    }
                }

                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
                File mediaFile;
                String mImageName = "MG_" + timeStamp + "" + i + ".jpg";
                mediaFile = new File(Environment.getExternalStorageDirectory() + File.separator + "/SVSM/" + mImageName);
                FileOutputStream fos1 = null;
                try {
                    fos1 = new FileOutputStream(mediaFile);
                    bmOut.compress(Bitmap.CompressFormat.JPEG, 90, fos1);
                    fos1.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                txtResultBody2.post(new Runnable() {
                    public void run() {
                        Frame frame = new Frame.Builder().setBitmap(bmOut).build();
                        SparseArray<Barcode> barCodes = barcodeDetector.detect(frame);
                        for (int index = 0; index < barCodes.size(); index++) {
                            txtResultBody2.setText("Finish");
                            Barcode code = barCodes.valueAt(index);
                            String data = verifyData(code.displayValue);
                            txtResultBody2.setText(txtResultBody2.getText() + "\n" + data + "\n");
                            status = true;
                            return;
                        }
                        if (barCodes.size() == 0) {
                            txtResultBody2.setText("No barcode could be detected. Please try again.");
                        }
                    }
                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Detector initialisation failed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile(int i) {
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "SVSM");

        if (!folder.exists()) {
            folder.mkdirs();
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + "" + i + ".jpg";
        mediaFile = new File(Environment.getExternalStorageDirectory() + File.separator + "/SVSM/" + mImageName);

        return mediaFile;


    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri,Bitmap bitmap) throws FileNotFoundException {

        /*ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);*/

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


    public String verifyData(String data) {
        boolean verify = false;
        String[] values = data.split(",");
        try {
            if (values.length >= 3) {
                InputStream keyFile = getAssets().open(values[0] + ".pub");
                PublicKey publicKey = SecurityHelper.getPublicKey(keyFile);
                verify = SecurityHelper.verify(publicKey, values[1], values[2]);
            } else {
                verify = false;
            }

        } catch (IOException e) {
            values[1] = "user not subscribed to service " + "\n" + values[1];
            return values[1];
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return verify ? "The signature is authentic" + values[1] : "The signature is not authentic." + values[0];
    }


}
