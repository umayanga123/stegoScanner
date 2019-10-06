#include <jni.h>
#include <string>

#include <android/log.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

bool isBitSet(uchar i, int i1);

using namespace std;
using namespace cv;


extern "C" JNIEXPORT jstring JNICALL
Java_com_stego_stegoscanner_MainActivity_11_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_stego_stegoscanner_MainActivity_12_adaptiveThresholdFromJNI(JNIEnv *env, jobject thiz,
                                                                     jlong inputAdress, jlong outputAddress) {
    Mat &input = *(Mat*) inputAdress;
    Mat &output = *(Mat*) outputAddress;

    clock_t begin = clock();

    cv::adaptiveThreshold(input, output, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY_INV, 21, 5);

    double total_time = double (clock() - begin ) / CLOCKS_PER_SEC;
    __android_log_print(ANDROID_LOG_INFO, "TAG", "adaptiveThreshold computation time = %f seconds\n",  total_time);
    return  env->NewStringUTF(to_string(total_time).c_str());
}


// Checks whether the bit is set or not at a particular position.
// Returns true if set
// Returns false if not set
bool isBitSet(uchar ch, int pos) {
    // 7 6 5 4 3 2 1 0
    ch = ch >> pos;
    if (ch & 1) {
        return true;
    }
    return false;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_stego_stegoscanner_MainActivity_12_LSB_1decoder(JNIEnv *env, jobject thiz, jlong frame) {
    // Stores original image
    Mat &image = *(Mat*) frame;
    if (image.empty()) {
        return env->NewStringUTF("ERROR");
    }

    std::string msg;
    // char to work on
    char ch = 0;
    // contains information about which bit of char to work on
    int bit_count = 0;


    //To extract the message from the image, we will iterate through the pixels and extract the LSB of
    //the pixel values (RGB) and this way we can get our message.

    for (int row = 0; row < image.rows; row++) {
        for (int col = 0; col < image.cols; col++) {
            for (int color = 0; color < 3; color++) {
                // stores the pixel details
                cv::Vec3b pixel = image.at<cv::Vec3b>(cv::Point(row, col));

                // manipulate char bits according to the LSB of pixel values
                if (isBitSet(pixel.val[color], 0)) {
                    ch |= 1;
                }
                // increment bit_count to work on next bit
                bit_count++;
                // bit_count is 8, that means we got our char from the encoded image
                if (bit_count == 8) {
                    // NULL char is encountered
                    if (ch == '\0') {
                        goto L2;
                    }
                    bit_count = 0;
                    msg += ch;
                    ch = 0;
                }else {
                    ch = ch << 1;
                }

            }
        }
    }L2:;


    image.release();
    return env->NewStringUTF(msg.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_stego_stegoscanner_PictureBarcodeActivity_LSB_1decoder(JNIEnv *env, jobject thiz,
                                                                jlong frame) {
    /// Stores original image
    Mat &image = *(Mat*) frame;
    if (image.empty()) {
        return env->NewStringUTF("ERROR");
    }

    std::string msg;
    // char to work on
    char ch = 0;
    // contains information about which bit of char to work on
    int bit_count = 0;


    //To extract the message from the image, we will iterate through the pixels and extract the LSB of
    //the pixel values (RGB) and this way we can get our message.

    for (int row = 0; row < image.rows; row++) {
        for (int col = 0; col < image.cols; col++) {
            for (int color = 0; color < 3; color++) {
                // stores the pixel details
                cv::Vec3b pixel = image.at<cv::Vec3b>(cv::Point(row, col));

                // manipulate char bits according to the LSB of pixel values
                if (isBitSet(pixel.val[color], 0)) {
                    ch |= 1;
                }
                // increment bit_count to work on next bit
                bit_count++;
                // bit_count is 8, that means we got our char from the encoded image
                if (bit_count == 8) {
                    // NULL char is encountered
                    if (ch == '\0') {
                        goto L2;
                    }
                    bit_count = 0;
                    msg += ch;
                    ch = 0;
                }else {
                    ch = ch << 1;
                }

            }
        }
    }L2:;


    image.release();
    return env->NewStringUTF(msg.c_str());
}