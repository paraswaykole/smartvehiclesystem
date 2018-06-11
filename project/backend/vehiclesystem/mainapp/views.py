from django.shortcuts import render
from django.http import HttpResponse,HttpResponseRedirect
from django.http import JsonResponse
from django.template import loader
from django.views.decorators.csrf import csrf_exempt
import json
from django.contrib.auth.models import User

# Create your views here.
from mainapp.models import Driver,Ride,RideData,Person

SPEEDLIMIT = 6

@csrf_exempt
def register(request):

    name = request.POST.get('name',False)
    phone = request.POST.get('phone',False)
    password = request.POST.get('password',False)
    vehicle_model = request.POST.get('vehicle_model',False)
    vehicle_number = request.POST.get('vehicle_number',False)
    city = request.POST.get('city',False)
    person = Person.objects.get(id=1)


    if name == False or phone == False or password == False or vehicle_number == False or vehicle_model ==  False or city == False:
        return JsonResponse({'result':False})

    new_driver = Driver(driver_name=name,driver_phone=phone,driver_vehicle_model=vehicle_model,driver_vehicle_number=vehicle_number,driver_password=password,driver_city=city,driver_admin=person)
    new_driver.save()

    return JsonResponse({'result':True,'userid':new_driver.id})

@csrf_exempt
def login(request):

    username = request.POST.get('username',False)
    password = request.POST.get('password',False)

    if username == False or password == False:
        return JsonResponse({'result':False})

    try:
        driver = Driver.objects.get(driver_vehicle_number=username,driver_password=password)
        return JsonResponse({'result':True,'userid':driver.id})
    except Driver.DoesNotExist:
        return JsonResponse({'result':False})

@csrf_exempt
def startride(request):

    driverid = request.POST.get('driverid',False)

    if driverid == False:
        return JsonResponse({'result':False})

    try:
        driver = Driver.objects.get(id=driverid)
    except Driver.DoesNotExist:
        return JsonResponse({'result':False})

    ride = Ride(ride_driver=driver)
    ride.save()

    return JsonResponse({'result':True,'rideid':ride.id})

@csrf_exempt
def ridedata(request):

    rideid = request.POST.get('rideid',False)
    speed = request.POST.get('speed',False)
    location = request.POST.get('location',False)

    if rideid == False or speed==False or location==False:
        return JsonResponse({'result':False})

    from random import randint
    speed = int(speed)

    try:
        ride = Ride.objects.get(id=rideid)
    except Ride.DoesNotExist:
        return JsonResponse({'result':False})

    rd = RideData(ridedata_ride=ride,ridedata_speed=speed,ridedata_location=location)
    rd.save()

    return JsonResponse({'result':True})

@csrf_exempt
def get_current_data(request):

    rideid = request.POST.get('rideid',False)
    if rideid == False:
        return JsonResponse({'result':False})

    try:
        ride = Ride.objects.get(id=rideid)
    except Ride.DoesNotExist:
        return JsonResponse({'result':False})

    rdata = RideData.objects.filter(ridedata_ride=ride).order_by('-ridedata_datetime')[:5]
    if len(rdata)==0:
        return JsonResponse({'result':False})

    speedlimit = SPEEDLIMIT
    status = 'safe'
    if rdata[0].ridedata_speed > speedlimit:
        status = 'overspeeding'

    return JsonResponse({'result':True,'speed':rdata[0].ridedata_speed,'speedlimit':speedlimit,'status':status})



#Dashboard views
def dashboard_login(request):

    user = request.POST.get('user',False)
    passs = request.POST.get('pass',False)

    if user != False and passs!=False:
        from django.contrib.auth import authenticate, login
        u = authenticate(username=user, password=passs)
        if u is not None:
            login(request, u)
            return HttpResponseRedirect('/dashboard/')

    context = {}
    template = loader.get_template('login.html')
    return HttpResponse(template.render(context, request))

def dashboard_register(request):

    user = request.POST.get('user',False)
    passs = request.POST.get('pass',False)

    if user != False and passs!=False:
        user = User.objects.create_user(user, user+'@vehiclesystem.com', passs)
        user.save()
        person = Person(person_user=user,person_city='Pune')
        person.save()
        return HttpResponseRedirect('/dashboard/login/')



    context = {}
    template = loader.get_template('register.html')
    return HttpResponse(template.render(context, request))

def dashboard_logout(request):
    from django.contrib.auth import logout
    logout(request)
    return HttpResponseRedirect('/dashboard/login/')


def dashboard_index(request):

    user = request.user
    if not user.is_authenticated():
        return HttpResponseRedirect('/dashboard/login/')

    person = Person.objects.get(person_user=user)
    rd = Driver.objects.filter(driver_admin=person)

    driverlist = []
    for x in rd:
        d = {}
        d['id'] = x.id
        d['name'] = x.driver_name
        d['vehicle_number'] = x.driver_vehicle_number

        from datetime import datetime, timedelta, time
        today = datetime.now().date()
        tomorrow = today + timedelta(1)
        today_start = datetime.combine(today, time())
        today_end = datetime.combine(tomorrow, time())
        rides = Ride.objects.filter(ride_driver=x,ride_datetime__lte=today_end,ride_datetime__gte=today_start)
        d['no_of_rides'] = len(rides)

        overspeeding_count = 0
        for r in rides:
            ridesdata = RideData.objects.filter(ridedata_ride=r,ridedata_speed__gt=SPEEDLIMIT)
            overspeeding_count += len(ridesdata)

        d['no_of_overspeeding'] = overspeeding_count
        driverlist.append(d)


    context = {'driver_list':driverlist}
    template = loader.get_template('dashboard.html')
    return HttpResponse(template.render(context, request))

def driverdetail_view(request):

    if not request.user.is_authenticated:
        return HttpResponseRedirect('/dashboard/login/')

    driverid = request.GET.get('did',False)
    if driverid == False:
        return HttpResponse('There was some problem!')

    try:
        driver = Driver.objects.get(id=driverid)
    except Driver.DoesNotExist:
        return HttpResponse('There was some problem!')


    from datetime import datetime, timedelta, time
    today = datetime.now().date()
    tomorrow = today + timedelta(1)
    today_start = datetime.combine(today, time())
    today_end = datetime.combine(tomorrow, time())
    rd = Ride.objects.filter(ride_driver=driver,ride_datetime__lte=today_end,ride_datetime__gte=today_start)

    ridedatalist = []
    for x in rd:
        d = {}
        d['id'] = x.id

        sourcepoints = RideData.objects.filter(ridedata_ride=x).order_by('ridedata_datetime')
        if len(sourcepoints) > 1:
            d['source'] = sourcepoints[0].ridedata_location
        else:
            d['source'] = 'Unknown'
        destpoints = RideData.objects.filter(ridedata_ride=x).order_by('-ridedata_datetime')
        if len(destpoints) > 1:
            d['dest'] = destpoints[0].ridedata_location
        else:
            d['dest'] = 'Unknown'
        d['time'] = str(x.ride_datetime)

        overspeeding_count = 0
        ridesdata = RideData.objects.filter(ridedata_ride=x,ridedata_speed__gt=SPEEDLIMIT)
        overspeeding_count += len(ridesdata)

        d['overspeeding'] = overspeeding_count
        ridedatalist.append(d)


    context = {'ridelist':ridedatalist}
    template = loader.get_template('driverdetails.html')
    return HttpResponse(template.render(context, request))

def ridemap_view(request):

    if not request.user.is_authenticated:
        return HttpResponseRedirect('/dashboard/login/')

    rideid = request.GET.get('rid',False)
    if rideid == False:
        return HttpResponse('There was some problem!')

    try:
        ride = Ride.objects.get(id=rideid)
    except Ride.DoesNotExist:
        return HttpResponse('There was some problem!')

    rdata = RideData.objects.filter(ridedata_ride=ride)
    if len(rdata)==0:
        return HttpResponse('There is no data for this ride!')

    lats = []
    lons = []
    labels = []
    for x in range(0,len(rdata)):
        speedlimit = SPEEDLIMIT
        if x==0:
            status = 'Start at '+str(rdata[x].ridedata_speed)+' km/h'
        elif x==len(rdata)-1:
            status = 'End at '+str(rdata[x].ridedata_speed)+' km/h'
        elif rdata[x].ridedata_speed > speedlimit:
            status = 'Overspeeding at '+str(rdata[x].ridedata_speed)+' km/h'
        else:
            status = 'safe at '+str(rdata[x].ridedata_speed)+' km/h'
        lats.append(float(rdata[x].ridedata_location.split(',')[0]))
        lons.append(float(rdata[x].ridedata_location.split(',')[1]))
        labels.append(status)

    context = {'latpoints':lats,'longpoints':lons,'labels':labels}
    template = loader.get_template('mapview.html')
    return HttpResponse(template.render(context, request))
