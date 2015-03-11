#include<iostream> 
#include<fstream> 
#include<unistd.h>
#include<string> 
#include<sstream> 
#include<stdlib.h>
#include<curl/curl.h>
#include"../GPIO.h"
#include"../http.h"
using namespace std;
#define LDR_PATH "/sys/bus/iio/devices/iio:device0/in_voltage"
#define GPIO_PATH "/sys/class/gpio/"
#define OUTPUT false
#define INPUT true
#define HIGH true
#define LOW false

float const CURRENT = 0.00068;
float const SLOPE_TEMPERATURE = 2;
float const OFFSET_TEMPERATURE = 1870;
float const SLOPE_PH = -0.070;
float const OFFSET_PH = 0.6;

class Sensor
{
	int number;


public:
	float m;
	float q;
	Sensor(int x): number(x){}
	void set_slope(float slope){
		m = slope;
	}
	void set_offset(float offset){
		q = offset;
	}
	float get_sensor_value(){
		float voltage_value = this->get_voltage_value();
		float sensor_value = (voltage_value - q)/m;
		return sensor_value;
	}
private:
	float get_voltage_value(){
		stringstream ss;
		ss << LDR_PATH << number << "_raw";
		fstream fs;
		int adc_value;
		fs.open(ss.str().c_str(), fstream::in);
		fs >> adc_value;
		fs.close();
		float cur_voltage = adc_value * (1.80f/4096.0f);
		float actual_value;
		return cur_voltage;

	}
};

class Temperature_Sensor : public Sensor{
private:

public:
	Temperature_Sensor(int x) : Sensor(x) {}
	float get_sensor_value(){
		float voltage_value = this->get_voltage_value();
		float resistor_value = voltage_value / CURRENT;
		float sensor_value = (resistor_value - q)/m;
		return sensor_value;
	}
	void post_temperature_data(float data){
		post_data_sensor(0, data);
	}
};

class PH_Sensor : public Sensor{

public:
	PH_Sensor(int x) : Sensor(x) {}
	float get_sensor_value(Sensor offset_sensor){
			//Sensor offset_sensor(2);
			float voltage_value = this->get_voltage_value();
			q += offset_sensor.get_voltage_value();
			float sensor_value = (voltage_value - q)/m;
			return sensor_value;
		}

	void post_ph_data(float data){
		post_data_sensor(1, data);
	}
};

int main(int argc, char* argv[])
{
	GPIO temperature_disable(45);
	float slope_temperature;
	float slope_ph;
	float offset_temperature;
	float offset_ph;

	if (argc == 5)
	{
		slope_temperature = atoi(argv[1]);
		offset_temperature = atoi(argv[2]);
		slope_ph = atoi(argv[3]);
		offset_ph = atoi(argv[4]);
	}
	else
	{
		slope_temperature = SLOPE_TEMPERATURE;
		offset_temperature = OFFSET_TEMPERATURE;
		slope_ph = SLOPE_PH;
		offset_ph = OFFSET_PH;
	}

	//set the pin GPIO_45 as output
	temperature_disable.setDirection(OUTPUT);
	//Temporary disable the temperature
	temperature_disable.setValue(HIGH);

	Temperature_Sensor temperature_sensor(2);
	PH_Sensor ph_sensor(1);
	Sensor offset_sensor(2);

	temperature_sensor.set_slope(slope_temperature);
	temperature_sensor.set_offset(offset_temperature);
	ph_sensor.set_slope(slope_ph);
	ph_sensor.set_offset(offset_ph);

	int i= 0;
	for(i=0; i<100; i++)
	{
		temperature_disable.setValue(LOW);
		float ph = ph_sensor.get_sensor_value(offset_sensor);
		float temperature = temperature_sensor.get_sensor_value();
		temperature_disable.setValue(HIGH);
		cout << "The voltage value is: " << temperature << " V." << endl;
		temperature_sensor.post_temperature_data(temperature);
		ph_sensor.post_ph_data(ph);
		usleep(100000);
	}
	float temperature = temperature_sensor.get_voltage_value();
	cout << "The voltage value is: " << temperature << " V." << endl;
	return 0;
}
