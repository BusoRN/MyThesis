#ifndef HTTP
#define HTTP
#include<iostream>
#include<fstream>
#include<unistd.h>
#include<string>
#include<sstream>
#include<stdlib.h>
#include<curl/curl.h>

std::string const URL_POST = "http://planar-contact-601.appspot.com/sensor_values";
std::string const POST_DATA_TEMPERATURE =  "sensor=temperature&value=";
std::string const POST_DATA_PH =  "sensor=pH&value=";
std::string const POST_DATA_BEATING =  "sensor=beating&value=";


void post_data_sensor(int type, float value);

#endif // HTTP

