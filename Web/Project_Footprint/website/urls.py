from django.urls import path
from . import views

urlpatterns = [
    path('place_list/',views.place_list, name="place_list")
]