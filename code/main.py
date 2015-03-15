from flask import Flask

app = Flask(__name__)

from flask import request
from flask import make_response

from models import MESSAGES
from google.appengine.ext import ndb
from google.appengine.api import memcache
import logging
import datetime
import cloudstorage
import json

SECONDS_BETWEEN_UPDATES = 60
SENSOR_TIMEOUT_IN_SECONDS = 300
ELECTROVALVES_TIME_OUT = 1000
BUCKET_NAME = "/CENSURED/"
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'mp4'}
UPDATE_FLAG = 'database_updated_recently'

#DataPoint contains all the points that have to be plotted
class DataPoint(ndb.Model):
    #the y-axes value
    value = ndb.FloatProperty()
    #the x-axes value (autofilled with the current data)
    date = ndb.DateTimeProperty(auto_now_add=True)

    @classmethod
    # returns the list of DataPoint associated with sensor_key
    def points_for_sensor(cls, sensor_key):
        return cls.query(ancestor=sensor_key).order(-cls.date)

    @classmethod
    #returns the last DataPoint inserted
    def oldest_points(cls):
        return cls.query().order(+cls.date)

#Sensor class contains the list of used sensors
class Sensor(ndb.Model):
    @classmethod
    # returns that list
    def sensor_list(cls):
        return cls.query()
 

def update():
    # Check to see if we have updated recently
    update_flag = memcache.get(UPDATE_FLAG)
    if update_flag is not None:
        return
    memcache.add(UPDATE_FLAG, 'YES', SECONDS_BETWEEN_UPDATES)
    # Delete old data points
    now = datetime.datetime.now()
    count = 0
    for point in DataPoint.oldest_points().iter():
        delta = (now - point.date).total_seconds()
        if delta > 3600:
            point.key.delete()
            count += 1
        else:
            break
    logging.info("Deleted {} old data points".format(count))
    # Loop through all of the sensors
    sensor_names = []
    for sensor in Sensor.sensor_list().iter():
        # Store current values of each sensor
        sensor_value = memcache.get(sensor.key.id() + "_value")
        if sensor_value is not None:
            point = DataPoint(parent=sensor.key, value=sensor_value)
            point.put()
        # Delete sensors that don't have any data
        curr_points = DataPoint.points_for_sensor(sensor.key)
        if curr_points.count() == 0 and sensor_value is None:
            logging.info("Deleting sensor {}".format(sensor.key.id()))
            sensor.key.delete()
        else:
            sensor_names.append(sensor.key.id())
    # Store a list of sensor names in memcache
    memcache.set("sensor_names", ",".join(sensor_names))

# this method is used to obtain the json format containg the list of the 
# entire sensor list made by {value, time, sensor_name} when the http 
# method is GET or to load a new value for a given sensor using the POST
# http method. In this last case the data to be passed with the POST are 
# "sensor", which indicates the sensor name, and "value", which is the
# float number of the sensor's value.
@app.route('/', methods=['POST'])
@app.route('/sensor_values', methods=['GET', 'POST'])
def process_values():
    if request.method == 'GET':
        sensor_names = memcache.get('sensor_names')
        if sensor_names is None:
            sensor_names = ",".join([sensor.key.id() for sensor in 
				     Sensor.sensor_list()])
            memcache.set('sensor_names', sensor_names)
        sensor_names = sensor_names.split(",")
        sensor_values = {}
        for sensor_name in sensor_names:
            sensor_value = memcache.get(sensor_name + '_value')
            if sensor_value is not None:
                sensor_values[sensor_name] = sensor_value
        return json.dumps(sensor_values)
    elif request.method == 'POST':
        update()
        logging.info(request.values)
        sensor_name = request.form.get('sensor')
        sensor_value = float(request.form.get('value'))
        sensor_names = memcache.get('sensor_names')
        if sensor_names is not None and sensor_name not in sensor_names:
            sensor = Sensor()
            sensor.key = ndb.Key('Sensor', sensor_name)
            sensor.put()
            sensor_names = sensor_name if sensor_names == "" else sensor_names + 
                           "," + sensor_name
            memcache.set('sensor_names', sensor_names)
        memcache.set(sensor_name + '_value', sensor_value, 
                     SENSOR_TIMEOUT_IN_SECONDS)
        return "Success\n"

# this function is accessible only using a http GET method, it returns a 
# json object with the sensor's names list
@app.route('/sensor_names', methods=['GET'])
def print_names():
    return json.dumps([sensor.key.id() for sensor in Sensor.sensor_list()])

# this function is accessible only through GET http method. This GET request
# has to contain two parameters one is  "sensor", the sensor name, and the 
# other one is "first_timestamp", the timestamp of the first  point that has 
# to be plotted (this last parameter in not mandatory, if missed it is assumed 
# equal to 0.
@app.route('/graphing_data', methods=['GET'])
def graphing_data():
    first_timestamp = request.args.get('first_timestamp')
    sensor_name = request.args.get('sensor')
    if first_timestamp is None:
        first_timestamp = 0
    else:
        first_timestamp = float(first_timestamp)
    points = DataPoint.points_for_sensor(ndb.Key('Sensor', sensor_name))
    epoch = datetime.datetime(1970, 1, 1)
    res = []
    for point in points.iter():
        timestamp = (point.date - epoch).total_seconds()
        if timestamp > first_timestamp:
            _res = {"timestamp": timestamp, "value": point.value}
            res.append(_res)
        else:
            break
    res.reverse()
    return json.dumps(res)

# this function is accessible through GET http method and deletes all the
# point of sensors
@app.route('/clear', methods=['GET'])
def clear_data():
    points = DataPoint.query()
    for point in points:
        point.key.delete()
    for sensor in Sensor.sensor_list():
        sensor.key.delete()
    return "Success"

# this function is accessible through both GET and POST http methods and it 
# is in charge to store the picture of the beating plot. This function is 
# normally used with the POST method where the image is passed through 
# "Image.jpg" field. Using a PC is also possible to update an image
# just using a browser, thank to the GET method implementation
@app.route('/picture/submit', methods=['GET', 'POST'])
def set_picture():
    if request.method == 'POST':

        _file = request.files['Image.jpg']

        if _file:
            cloud_file = cloudstorage.open(BUCKET_NAME + "microscope_image." 
                                           + _file.filename.rsplit('.', 1)[1],
                                           mode='w', content_type="image/jpeg")
            _file.save(cloud_file)
            cloud_file.close()
            return "Success"
    else:
        return '''
        <!doctype html>
        <title>Upload microscope file V.1</title>
        <h1>Upload microscope file</h1>
        <form method="POST"
            action=""
            role="form"
            enctype="multipart/form-data">

        <p><input type=file name=Image.jpg>
           <input type=submit value=Upload>
        </form>
        '''

# this function is accessible through a GET http method and it returns the image 
# stored inside the datastore
@app.route('/picture/view', methods=['GET'])
def view_picture():
    for _file in cloudstorage.listbucket(BUCKET_NAME):
        if "microscope_image" in _file.filename:
            _file = cloudstorage.stat(_file.filename)
            logging.info(_file.filename)
            logging.info(_file.content_type)
            cloud_file = cloudstorage.open(_file.filename, mode='r')
            response = make_response(cloud_file.read())
            cloud_file.close()
            response.mimetype = _file.content_type
            return response
    return "No file found"

# this function is accessible through both GET and POST http methods and it 
# is in charge to store the microscope video. This function is normally used 
# with the POST method where the image is passed through "Video.mp4" field. 
# Using a PC is also possible to update a video just using a browser, 
# thank to the GET method implementation
@app.route('/video/submit', methods=['GET', 'POST'])
def set_video():
    if request.method == 'GET':
        return '''
        <!doctype html>
        <title>Upload microscope video</title>
        <h1>Upload microscope video</h1>
        <form method="POST"
             action=""
             role="form"
             enctype="multipart/form-data">
         <p><input type=file name=Video.mp4>
         <input type=submit value=Upload>
         </form>
         '''
    else:
        _file = request.files['Video.mp4']
        if _file:
            cloud_file = cloudstorage.open(BUCKET_NAME + "microscope_video." + 
                                           _file.filename.rsplit('.', 1)[1],
                                           mode='w', content_type="video/mpeg")
            _file.save(cloud_file)
            cloud_file.close()
        return "Success"


# this function is accessible through a GET http method and it returns the 
# microscope video stored inside the datastore
@app.route('/video/view', methods=['GET'])
def view_video():
    for _file in cloudstorage.listbucket(BUCKET_NAME):
        if "microscope_video" in _file.filename:
            _file = cloudstorage.stat(_file.filename)
            logging.info(_file.filename)
            logging.info(_file.content_type)
            cloud_file = cloudstorage.open(_file.filename, mode='r')
            response = make_response(cloud_file.read())
            cloud_file.close()
            response.mimetype = _file.content_type
            return response
    return "No file found"

# this function is accessible through a POST http method and it is in 
# charge to store the electrovalve status inside the memcache. This http 
# POST has to have the following parameters: "name", the name that
# identifies the valve, "status", the status of electrovalve (on, off)
@app.route('/add/electrovalve', methods=['POST'])
def add_electrovalve():
    key = request.form.get('name')
    message = request.form.get('status')
    if key and message:
        if message == 'on' or message == 'On' or message == 'ON':
            MESSAGES[key] = True
            memcache.set(key, True)

        else:
            MESSAGES[key] = False
            memcache.set(key, False)

       # ev.put()
    return '%r' % memcache.get(key)

# this function accessible through a GET method returns the value of the 
# valve identified through <name> field in the URL
@app.route('/electrovalves/<name>', methods=['GET'])
def get_electrovalve(name):
    ev = memcache.get(name);
    if ev is None:
        return ' %r ' % MESSAGES[name] or "%s not found!" % name
    else:
        return ' %r ' % memcache.get(name)
      
@app.errorhandler(404)
def page_not_found(e):
    """Return a custom 404 error."""
    return 'Sorry, nothing at this URL.', 404



if __name__ == "__main__":
    app.run()

