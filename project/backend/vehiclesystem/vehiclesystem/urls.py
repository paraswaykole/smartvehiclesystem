"""vehiclesystem URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.9/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.conf.urls import url, include
    2. Add a URL to urlpatterns:  url(r'^blog/', include('blog.urls'))
"""
from django.conf.urls import url
from django.contrib import admin

from mainapp.views import register,login,startride,ridedata,get_current_data,dashboard_index,driverdetail_view,dashboard_login,dashboard_register
from mainapp.views import dashboard_logout,ridemap_view

urlpatterns = [
    url(r'^admin/', admin.site.urls),
    url(r'^driverapp/register/',register),
    url(r'^driverapp/login/',login),
    url(r'^driverapp/startride/',startride),
    url(r'^driverapp/currentdata/',get_current_data),
    url(r'^driverdevice/ridedata/',ridedata),
    url(r'^dashboard/$',dashboard_index),
    url(r'^dashboard/login/$',dashboard_login),
    url(r'^dashboard/register/$',dashboard_register),
    url(r'^dashboard/logout/$',dashboard_logout),
    url(r'^dashboard/driver/$',driverdetail_view),
    url(r'^dashboard/map/$',ridemap_view),
]
