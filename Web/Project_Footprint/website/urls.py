from django.urls import path
from . import views

urlpatterns = [
    path('history/', views.history, name='history'),
    path('place_list/', views.place_list, name='place_list'),
    path('place_register/', views.place_register, name='place_register'),
    path('index/', views.index, name='index'),
    path('place_restaurant_list/', views.place_restaurant, name='place_restaurant_list'),
    path('place_sights_list/', views.place_sights, name='place_sights_list'),
]
