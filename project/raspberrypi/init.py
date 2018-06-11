#!/usr/bin/python3
import RPi.GPIO as GPIO
from time import sleep
import time,math,json,requests

dist_meas = 0.00
km_per_hour = 0
rpm = 0
elapse = 0
sensor = 24
pulse = 0
location = ' '
start_timer = time.time()
server_timer = time.time()


with open('/home/pi/server/data.json','w') as file:
	json.dump({"isconnected":False},file)


def init_GPIO():					# initialize GPIO
	GPIO.setmode(GPIO.BCM)
	GPIO.setwarnings(False)
	GPIO.setup(sensor,GPIO.IN)

def calculate_elapse(channel):				# callback function
	global pulse, start_timer, elapse
	pulse+=1								# increase pulse by 1 whenever interrupt occurred
	elapse = time.time() - start_timer		# elapse for every 1 complete rotation made!
	start_timer = time.time()				# let current time equals to start_timer

def calculate_speed(r_cm):
	global pulse,elapse,rpm,dist_km,dist_meas,km_per_sec,km_per_hour
	if elapse !=0:							# to avoid DivisionByZero error
		rpm = 1/elapse * 60
		circ_cm = (2*math.pi)*r_cm			# calculate wheel circumference in CM
		dist_km = circ_cm/100000 			# convert cm to km
		km_per_sec = dist_km / elapse		# calculate KM/sec
		km_per_hour = km_per_sec * 3600		# calculate KM/h
		dist_meas = (dist_km*pulse)*1000	# measure distance traverse in meter
		return km_per_hour

def send_data_to_server():
	global server_timer
	with open('/home/pi/server/data.json') as outfile:
                data = json.load(outfile)
	if 'isdriving' not in data or 'location' not in data:
		return
	if not data['isdriving']:
		return
	elapsedtime = time.time() - server_timer
	if elapsedtime < 3:
		return
	server_timer = time.time()
	senddata = {'rideid':data['rideid'],'speed':str(int(km_per_hour)),'location':data['location']}
	r = requests.post('http://192.168.43.32:8000/driverdevice/ridedata/', data = senddata)
	#print(r.text)
	#print('Rideid:'+data['rideid']+',Speed:'+str(int(km_per_hour))+',Location:'+data['location'])

def init_interrupt():
	GPIO.add_event_detect(sensor, GPIO.FALLING, callback = calculate_elapse, bouncetime = 20)

if __name__ == '__main__':
	init_GPIO()
	init_interrupt()
	while True:
		calculate_speed(4)	# call this function with wheel radius as parameter
		send_data_to_server()
		#print('rpm:{0:.0f}-RPM kmh:{1:.0f}-KMH dist_meas:{2:.2f}m pulse:{3}'.format(rpm,km_per_hour,dist_meas,pulse))
		sleep(0.1)
