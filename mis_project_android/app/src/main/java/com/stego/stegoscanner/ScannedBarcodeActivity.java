package com.stego.stegoscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;

public class ScannedBarcodeActivity extends AppCompatActivity {


    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    Button btnAction;
    String intentData = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        initViews();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        surfaceView = findViewById(R.id.surfaceView);
        btnAction = findViewById(R.id.btnAction);

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intentData.length() > 0 &&  URLUtil.isValidUrl(intentData)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intentData)));
                }
            }
        });
    }

    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();


        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScannedBarcodeActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        ScannedBarcodeActivity.this.cameraSource.start(surfaceView.getHolder());

                    } else {
                        ActivityCompat.requestPermissions(ScannedBarcodeActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
             }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> barCodes = detections.getDetectedItems();
                if (barCodes.size() != 0) {
                    txtBarcodeValue.post(new Runnable() {
                        @Override
                        public void run() {
                            intentData = barCodes.valueAt(0).displayValue;
                            String data = verifyData(intentData);
                            txtBarcodeValue.setText(data);
                        }
                    });

                }
            }
        });
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

        return verify ? "The signature is authentic. " + values[1] : "The signature is not authentic." + values[0];
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(cameraSource!=null){
            cameraSource.release();
        }
        System.gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.gc();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                cameraSource.release();
                return true;
            default:
                cameraSource.release();
                return super.onOptionsItemSelected(item);
        }
    }
}
