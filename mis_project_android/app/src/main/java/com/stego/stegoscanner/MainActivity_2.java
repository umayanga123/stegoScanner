package com.stego.stegoscanner;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity_2 extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private final String TAG = "MainActivity_2";


    JavaCamera2View mOpenCvCameraView = null;
    Mat mRgba,imgGray,imgCany;
    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        public void onManagerConnected(int status) {
            switch(status) {
                case BaseLoaderCallback.SUCCESS:
                    mOpenCvCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        mOpenCvCameraView = findViewById(R.id.main_surface);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mOpenCvCameraView!=null){
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mOpenCvCameraView!=null){
            mOpenCvCameraView.disableView();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.d(TAG,"OpenCV loadded");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else{
            Log.d(TAG,"opencv not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,mLoaderCallback);
        }
    }



    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba =new Mat(width,height, CvType.CV_8UC4);
        imgGray =new Mat(width,height, CvType.CV_8UC1);
        imgCany =new Mat(width,height, CvType.CV_8UC1);


    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        List<Mat> bgr = new ArrayList<>();
        Core.split(inputFrame.rgba(), bgr);
        Mat one_ch_image = bgr.get(0);
        double[] doubles = one_ch_image.get(one_ch_image.rows(), one_ch_image.cols());
        for(int i = 0 ; doubles.length > i;i++){
            double v = doubles[i];
            if(v >128){
                doubles[i] =255;
            }else {
                doubles[i] = 0;
            }
        }
        return new Mat(one_ch_image.rows(),one_ch_image.cols(),doubles.hashCode());



        //return getBwImage(one_ch_image);

    }

    public native Mat getBwImage(Mat input);

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

}