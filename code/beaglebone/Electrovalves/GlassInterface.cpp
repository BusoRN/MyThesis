#include <stdio.h>
#include <string.h>
#include <iostream>
#include <sstream>
#include <curl/curl.h>
#include <GPIO/GPIO.h>

#define NUMBER_ELECTROVALVES 8

using namespace exploringBB;
using namespace std;

std::string const ELECTROVALVES_LINK = "http://planar-contact-601.appspot.com/show/";
std::string const TRUE_VALUE = " True ";
std::string const FALSE_VALUE = " False ";

static size_t WriteCallback(void *contents, size_t size, size_t nmemb, void *userp)
{
	((std::string*)userp)->append((char*)contents, size * nmemb);
	return size * nmemb;
}


int main(void)
{
	// constructor of pins
	GPIO EV1(44), EV2(23), EV3(26), EV4(47), EV5(46), EV6(27), EV7(65), EV8(22);
        GPIO RELAY(61);

	CURL *curl;
	CURLcode res;

	GPIO_VALUE output_vector[NUMBER_ELECTROVALVES];

	//Set output pins
	EV1.setDirection(OUTPUT);
	EV2.setDirection(OUTPUT);
	EV3.setDirection(OUTPUT);
	EV4.setDirection(OUTPUT);
	EV5.setDirection(OUTPUT);
	EV6.setDirection(OUTPUT);
	EV7.setDirection(OUTPUT);
	EV8.setDirection(OUTPUT);

	RELAY.setDirection(OUTPUT);

	RELAY.setValue(HIGH);

	while(1)
	{
		//acquire the values of electrovalves status and store them inside output_vector
		int i;
		for(i=0;i<NUMBER_ELECTROVALVES; i++)
		{
			curl = curl_easy_init();
			if(curl)
			{
				std::string readBuffer;
				std::ostringstream ss;
				ss << i+1;
				std::string url = ELECTROVALVES_LINK + "EV" + ss.str();
				curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
				/* example.com is redirected, so we tell libcurl to follow redirection */
				curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
				curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readBuffer);
				/* Perform the request, res will get the return code */
				res = curl_easy_perform(curl);
				/* always cleanup */
				curl_easy_cleanup(curl);

				if(readBuffer.compare(TRUE_VALUE) == 0)
				{
					std::cout << "EV" + ss.str()+": ON" << std::endl;
					output_vector[i] = HIGH;
				}
				else if(readBuffer.compare(FALSE_VALUE) == 0)
				{
					std::cout << "EV" + ss.str()+": OFF" << std::endl;
					output_vector[i] = LOW;
				}

			}

		}

		//now I'm ready to write the output pins:
		EV1.setValue(output_vector[0]);
		EV2.setValue(output_vector[1]);
		EV3.setValue(output_vector[2]);
		EV4.setValue(output_vector[3]);
		EV5.setValue(output_vector[4]);
		EV6.setValue(output_vector[5]);
		EV7.setValue(output_vector[6]);
		EV8.setValue(output_vector[7]);
	}


	return 0;
}




