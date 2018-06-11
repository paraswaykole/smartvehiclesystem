from __future__ import unicode_literals

from django.db import models
from django.contrib.auth.models import User

# Create your models here.
class Person(models.Model):
    person_user = models.OneToOneField(User)
    person_city = models.CharField(max_length=64)

class Driver(models.Model):
    driver_name = models.CharField(max_length=128)
    driver_phone = models.CharField(max_length=10)
    driver_vehicle_model = models.CharField(max_length=128)
    driver_vehicle_number= models.CharField(max_length=10)
    driver_password = models.CharField(max_length=32)
    driver_city = models.CharField(max_length=64)
    driver_admin = models.ForeignKey(Person)

class Ride(models.Model):
    ride_driver = models.ForeignKey(Driver)
    ride_overspeedcount = models.IntegerField(default=0)
    ride_datetime = models.DateTimeField(auto_now_add=True)

class RideData(models.Model):
    ridedata_ride = models.ForeignKey(Ride)
    ridedata_speed = models.IntegerField(default=-1)
    ridedata_location = models.CharField(max_length=128)
    ridedata_datetime = models.DateTimeField(auto_now_add=True)
