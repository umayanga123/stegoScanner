package com.stego.stegoscanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnTakePicture, btnScanBarcode ,btnExtractInformation ,btnExit;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        btnTakePicture = findViewById(R.id.btnTakePicture);
        btnScanBarcode = findViewById(R.id.btnScanBarcode);
        btnExtractInformation= findViewById(R.id.btnTakeInfo);
        btnExit   =findViewById(R.id.btnExit);
        imageView =findViewById(R.id.imageViewTwo);
        btnTakePicture.setOnClickListener(this);
        btnScanBarcode.setOnClickListener(this);
        btnExtractInformation.setOnClickListener(this);
        btnExit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnTakePicture:
                startActivity(new Intent(MainActivity.this, PictureBarcodeActivity.class));
               break;
            case R.id.btnScanBarcode:
                startActivity(new Intent(MainActivity.this, ScannedBarcodeActivity.class));
                break;
            case R.id.btnTakeInfo:
                startActivity(new Intent(MainActivity.this, CameraAppActivity.class));
                break;
            case R.id.btnExit:
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
        }

    }


}
