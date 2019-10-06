package com.stego.stegoscanner;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_COLOR;
import static org.opencv.imgcodecs.Imgcodecs.imread;

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

        //

        /*try {
            Mat img = Utils.loadResource(getApplicationContext(), R.drawable.a, CV_LOAD_IMAGE_COLOR);
            String lsb_decoder = LSB_decoder(img.getNativeObjAddr());
            Toast.makeText(this, lsb_decoder, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }*/




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

       /** try {
            Mat img = Utils.loadResource(getApplicationContext(), R.drawable.a, CV_LOAD_IMAGE_COLOR);
            String lsb_decoder = LSB_decoder(img.getNativeObjAddr());
            Toast.makeText(this, lsb_decoder, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }**/

        Mat result = inputFrame.rgba();

      //  String lsb_decoder = LSB_decoder(result.getNativeObjAddr());
       // Toast.makeText(this, lsb_decoder, Toast.LENGTH_LONG).show();

       // String s = adaptiveThresholdFromJNI(inputFrame.gray().getNativeObjAddr(), result.getNativeObjAddr());
        //Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
       // ----
        //mRgba =inputFrame.rgba();

       /// Imgproc.cvtColor(mRgba,imgGray,Imgproc.COLOR_RGB2BGRA);
       // Imgproc.Canny(imgGray,imgCany,50,150);

      //  String lsb_decoder = LSB_decoder(result.getNativeObjAddr());
       // Toast.makeText(this, lsb_decoder, Toast.LENGTH_SHORT).show();

        return result;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String adaptiveThresholdFromJNI(long input, long output);

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public native  String LSB_decoder(long  frame) ;

}