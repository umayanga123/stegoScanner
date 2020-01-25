#include <jni.h>
#include <string>

#include <android/log.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>


using namespace std;
using namespace cv;

extern "C"
JNIEXPORT Mat JNICALL
Java_com_stego_stegoscanner_PictureBarcodeActivity_decodeFrame(JNIEnv *env, jobject thiz,
                                                                  jobject input, jobject output) {
    // TODO: implement decodeFrame()
    Mat &inputI = *(Mat *) input;
    Mat &outputI = *(Mat *) output;

    cv::Mat rgb[4] ;

    // The actual splitting
    split(inputI, rgb);

    Mat *newMath = &rgb[0];

    return *newMath;

}



extern "C"
JNIEXPORT Mat JNICALL
Java_com_stego_stegoscanner_MainActivity_12_getBwImage(JNIEnv *env, jobject thiz, jobject inputAdress) {
    Mat &one_ch_image = *(Mat *) inputAdress;
    cv::Mat img_bw;

    //convert to BW
    if (false) {
        one_ch_image.copyTo(img_bw);
        for (int y = 0; y < one_ch_image.cols; y++) {
            for (int x = 0; x < one_ch_image.rows; x++) {
                int value =(int)one_ch_image.at<uchar>(x, y);
                if (value != 0) {
                    img_bw.at<uchar>(x, y) = 255;
                }
                else {
                    img_bw.at<uchar>(x, y) = 0;
                }
            }
        }
    }
    else {
        img_bw = one_ch_image > 128;
    }

    return  img_bw;
}