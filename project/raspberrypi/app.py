from flask import Flask
from flask import request
import json

app = Flask(__name__)

@app.route("/connection",methods=['GET'])
def connection():
	uid = request.args.get('u')
	file_data = load_file_data()
	file_data['isconnected'] = True
	file_data['userid'] = uid
	save_file_data(file_data)
	return "it works"


@app.route("/startdriving",methods=['GET'])
def startdriving():
	rid = request.args.get('r')
	file_data = load_file_data()
	file_data['rideid'] = rid
	file_data['isdriving'] = True
	save_file_data(file_data)
	return "true"

@app.route("/enddriving",methods=['GET'])
def enddriving():
        file_data = load_file_data()
        file_data['isdriving'] = False
        save_file_data(file_data)
        return "true"

@app.route("/newlocation",methods=['GET'])
def newlocation():
	loc = request.args.get('loc')
        file_data = load_file_data()
        file_data['location'] = loc
        save_file_data(file_data)
        return "true"


@app.route("/shutdown")
def shutdown():
	file_data = load_file_data()
        if 'isconnected' in file_data:
                file_data['isconnected'] = False
                save_file_data(file_data)
	shutdown_server()
	return "shutting down server"

def load_file_data():
	with open('/home/pi/server/data.json') as file:
		file_data = json.load(file)
	return file_data

def save_file_data(file_data):
	with open('/home/pi/server/data.json','w') as outfile:
		json.dump(file_data,outfile)

def shutdown_server():
        func = request.environ.get('werkzeug.server.shutdown')
        if func is None:
                raise RuntimeError('Not Running with Werkzeug Server')
        func()

if __name__ == '__main__':
	app.run(debug=True,host='0.0.0.0',port=80)
