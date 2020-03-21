package com.stego.stegoscanner;

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;


import java.io.File;
import java.io.FileNotFoundException;

import androidx.annotation.NonNull;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

public class PictureBarcodeActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnOpenCamera;
    TextView txtResultBody;
    ImageView imageView;

    private BarcodeDetector detector;
    private Uri imageUri;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int CAMERA_REQUEST = 101;
    private static final String TAG = "API123";
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVED_INSTANCE_RESULT = "result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_picture);

        initViews();

        if (savedInstanceState != null) {
            if (imageUri != null) {
                imageUri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI));
                txtResultBody.setText(savedInstanceState.getString(SAVED_INSTANCE_RESULT));
            }
        }

        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();
        if (!detector.isOperational()) {
            txtResultBody.setText("Detector initialisation failed");
            return;
        }
    }

    private void initViews() {
        txtResultBody = findViewById(R.id.txtResultsBody);
        btnOpenCamera = findViewById(R.id.btnOpenCamera);
        btnOpenCamera.setOnClickListener(this);
        imageView = findViewById(R.id.imageView);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnOpenCamera:
                ActivityCompat.requestPermissions(PictureBarcodeActivity.this, new
                        String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    takeBarcodePicture();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            launchMediaScanIntent();
            try {

                Bitmap bitmap = decodeBitmapUri(this, imageUri);
                imageView.setImageBitmap(bitmap);
                txtResultBody.setText("Wait ...");

               // String someValue = "Just a demo, really...";
                new Thread(new Runnable() {
                    public   Bitmap bitmap;
                    public Runnable init(Bitmap myParam ) {
                        this.bitmap = myParam;
                        return this;
                    }
                    @Override
                    public void run() {
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


                        imageView.post(new Runnable() {
                            public void run() {
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90);
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmOut, bmOut.getWidth(), bmOut.getHeight(), true);
                                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                                imageView.setImageBitmap(rotatedBitmap);
                            }
                        });


                        txtResultBody.post(new Runnable() {
                            public void run() {
                                txtResultBody.setText("Finish");

                                //Detect bar code pattern
                                if (detector.isOperational() && bmOut != null) {
                                    Frame frame = new Frame.Builder().setBitmap(bmOut).build();

                                    SparseArray<Barcode> barcodes = detector.detect(frame);
                                    for (int index = 0; index < barcodes.size(); index++) {
                                        Barcode code = barcodes.valueAt(index);
                                        txtResultBody.setText(txtResultBody.getText() + "\n" + code.displayValue + "\n");

                                    }
                                    if (barcodes.size() == 0) {
                                        txtResultBody.setText("No barcode could be detected. Please try again.");
                                    }
                                } else {
                                    txtResultBody.setText("Detector initialisation failed");
                                }


                            }
                        });



                    }
                }.init(bitmap)).start();

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to load Image", Toast.LENGTH_SHORT)
                        .show();
                Log.e(TAG, e.toString());
            }
        }
    }

    private void takeBarcodePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "pic.jpg");
        imageUri = FileProvider.getUriForFile(PictureBarcodeActivity.this,
                BuildConfig.APPLICATION_ID + ".provider", photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (imageUri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageUri.toString());
            outState.putString(SAVED_INSTANCE_RESULT, txtResultBody.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
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
