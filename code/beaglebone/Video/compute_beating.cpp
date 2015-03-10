
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
	CURL *curl;
	CURLcode res;
	struct stat file_info;
	FILE *file_to_send;
	file_to_send = fopen("Video.mp4", "rb");
	double speed_upload, total_time;

	if(!file_to_send)
	{
		return 1;
	}
	//to get the file size
	if(fstat(fileno(file_to_send), &file_info) != 0)
	{
		return 1;
	}

	curl = curl_easy_init();
	if(curl)
	{
		curl_easy_setopt(curl, CURLOPT_URL,
				"http://planar-contact-601.appspot.com/video/submit");
		curl_easy_setopt(curl, CURLOPT_UPLOAD, 1L);
		curl_easy_setopt(curl,CURLOPT_READDATA, file_to_send);
		curl_easy_setopt(curl,CURLOPT_INFILESIZE_LARGE,
                (curl_off_t)file_info.st_size);
		curl_easy_setopt(curl, CURLOPT_VERBOSE, 1L);
		res = curl_easy_perform(curl);
		/* Check for errors */
		    if(res != CURLE_OK) {
		      fprintf(stderr, "curl_easy_perform() failed: %s\n",
		              curl_easy_strerror(res));

		    }
		    else {
		      /* now extract transfer info */
		      curl_easy_getinfo(curl, CURLINFO_SPEED_UPLOAD, &speed_upload);
		      curl_easy_getinfo(curl, CURLINFO_TOTAL_TIME, &total_time);

		      fprintf(stderr, "Speed: %.3f bytes/sec during %.3f seconds\n",
		              speed_upload, total_time);

		    }
//		    /* always cleanup */
		    curl_easy_cleanup(curl);
	}

    ofstream point_file;
    point_file.open("video_data.dat");
    VideoCapture capture;//(0);   // capturing from file
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
    float initial_point = (color_information[0] +color_information[1]+color_information[2])/3;

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
