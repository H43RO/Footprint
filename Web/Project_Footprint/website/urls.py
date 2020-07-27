from django.urls import path
from . import views

urlpatterns = [
    path('history/', views.history, name='history'),
    path('place_list/', views.place_list, name="place_list")
]
    