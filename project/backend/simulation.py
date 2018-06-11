import requests,json,time

url = "0.0.0.0:8000"

req = requests.post('http://'+url+'/driverapp/startride/', data = {'driverid':'1'})
result = json.loads(req.text)

rideid = False
if result['result']:
    rideid = result['rideid']

if rideid!=False:
    speeddata = [{'s':50,'l':'19.612756,74.182350'},{'s':100,'l':'19.612012,74.182524'},{'s':60,'l':'19.611213,74.182681'},{'s':98,'l':'19.610284,74.182811'}]
    for i in range(0,len(speeddata)):
        s = speeddata[i]['s']
        l = speeddata[i]['l']
        req = requests.post('http://'+url+'/driverdevice/ridedata/', data = {'rideid':str(rideid),'speed':str(s),'location':str(l)})
        result = json.loads(req.text)
        if result['result']:
            req = requests.post('http://'+url+'/driverapp/currentdata/', data = {'rideid':str(rideid)})
            result = json.loads(req.text)
            if result['result']:
                print(str(result['speed'])+":"+result['status'])
        else:
            print("Failed"+str(i))
        time.sleep(2)
