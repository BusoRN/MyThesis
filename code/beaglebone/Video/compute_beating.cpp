
#include<iostream>
#include<fstream>
#include<string>
#include <curl/curl.h>
#include <sys/stat.h>
#include <fcntl.h>
#include<sstream>
#include<opencv2/opencv.hpp>   // C++ OpenCV include file
using namespace std;
using namespace cv;            // using the cv namespace too

int main()
{
    ofstream point_file;
    point_file.open("video_data.dat");
    VideoCapture capture; // capturing from file
    capture.open("output.mp4");

    // set any  properties in the VideoCapture object
    capture.set(CV_CAP_PROP_FRAME_WIDTH,320);   // width pixels
    capture.set(CV_CAP_PROP_FRAME_HEIGHT,240);   // height pixels
    
    if(!capture.isOpened())
    {
        cout << "Capture not open." << endl;
    }

    Mat frame, firstFrame, diffFrame;

    capture >> frame; //save the first frame
    firstFrame = frame;
    Scalar color_information = mean(firstFrame);
    float initial_point = (color_information[0] +color_information[1]
	    +color_information[2])/3;

    int i;
    for(i=1; i<capture.get(CV_CAP_PROP_FRAME_COUNT);i++)
    {
        capture >> frame;          // capture the image to the frame
        if(frame.empty())
        {
            cout << "Failed to capture an image" << endl;
            return -1;
        }
        absdiff(frame, firstFrame, diffFrame);
        color_information = mean(frame);
        float red = color_information[0];
        float green = color_information[1];
        float blue = color_information[2];

        float mean_color = (red+green+blue)/3;

         //qui poi chiamo la funzione che mi posta il punto nel db
         float temp = (i-1); 
        temp *= 0.033;
	
	point_file << temp << '\t';
        point_file << mean_color << '\n';

     }


    point_file.close();
    return 0;
}
